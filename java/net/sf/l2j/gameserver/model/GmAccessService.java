package net.sf.l2j.gameserver.model;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.datatables.AccessLevels;
import net.sf.l2j.gameserver.datatables.AdminCommandAccessRights;
import net.sf.l2j.gameserver.datatables.GmListTable;
import net.sf.l2j.gameserver.datatables.xml.GmData;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.GMHolder;

public final class GmAccessService
{
	private static final Logger LOGGER = Logger.getLogger(GmAccessService.class.getName());
	
	private static final Path WATCH_DIR = Paths.get("./data/xml/custom/adminaccesslevel");
	private static final String WATCH_FILE = "gm_list.xml";
	
	// Delay maior para evitar reload enquanto o XML ainda está sendo salvo pelo editor.
	private static final long RELOAD_DEBOUNCE_MS = 1000;
	private static final long RELOAD_DELAY_MS = 1500;
	
	private WatchService _watchService;
	private Thread _watchThread;
	
	private final AtomicLong _lastReloadAt = new AtomicLong(0);
	private volatile ScheduledFuture<?> _pendingReload;
	
	private GmAccessService()
	{
		startWatcher();
	}
	
	public void shutdown()
	{
		stopWatcher();
	}
	
	public static void onEnterWorld(Player player)
	{
		if (player == null)
			return;
		
		// Garante que o singleton seja iniciado, inclusive o watcher.
		getInstance();
		
		if (Config.EVERYBODY_HAS_ADMIN_RIGHTS)
			return;
		
		// No login/enter world pode corrigir e também pode remover GM inválido.
		enforceForOnlinePlayer(player, "EnterWorld", true);
	}
	
	public static void reconcilePlayerByNameOrObj(String name, int objectId)
	{
		if (Config.EVERYBODY_HAS_ADMIN_RIGHTS)
			return;
		
		getInstance();
		
		Player online = null;
		
		if (objectId > 0)
			online = L2World.getInstance().getPlayer(objectId);
		
		if (online == null && name != null && !name.trim().isEmpty())
			online = L2World.getInstance().getPlayer(name);
		
		if (online != null)
		{
			// Reconciliação manual pode corrigir e remover accesslevel inválido.
			enforceForOnlinePlayer(online, "AdminReconcile", true);
		}
		else if (objectId > 0)
		{
			enforceForOfflineObjectId(objectId, "AdminReconcile");
		}
	}
	
	public static void reapplyAllOnline(String reason)
	{
		reapplyAllOnline(reason, false);
	}
	
	private static void reapplyAllOnline(String reason, boolean allowDemote)
	{
		if (Config.EVERYBODY_HAS_ADMIN_RIGHTS)
			return;
		
		for (Player p : L2World.getInstance().getPlayers())
		{
			try
			{
				enforceForOnlinePlayer(p, reason, allowDemote);
			}
			catch (Exception e)
			{
				LOGGER.log(Level.WARNING, "Failed enforce GM access for " + p.getName(), e);
			}
		}
	}
	
	private static void enforceForOnlinePlayer(Player player, String reason, boolean allowDemote)
	{
		final int objId = player.getObjectId();
		final int current = getAccessLevelNumber(player);
		
		final GMHolder entry = GmData.getInstance().getEntry(objId);
		
		if (entry == null || !entry.isEnabled() || entry.getAccessLevel() <= AccessLevels.USER_ACCESS_LEVEL_NUMBER)
		{
			// Durante reload automático do XML, nunca rebaixa GM.
			// Isso evita o bug do admin perder comandos quando o XML está sendo salvo/recarregado.
			if (!allowDemote)
			{
				if (current > AccessLevels.USER_ACCESS_LEVEL_NUMBER)
				{
					LOGGER.warning("GM access kept during safe reload: " + player.getName() + " objId=" + objId + " current=" + current + " reason=" + reason);
				}
				
				return;
			}
			
			// Só rebaixa quem realmente tem accesslevel acima de user.
			// Não mexe em players normais nem em banidos.
			if (current > AccessLevels.USER_ACCESS_LEVEL_NUMBER)
			{
				player.setAccessLevel(AccessLevels.USER_ACCESS_LEVEL_NUMBER);
				updateCharacterAccessLevel(objId, AccessLevels.USER_ACCESS_LEVEL_NUMBER);
				
				LOGGER.info("GM denied: " + player.getName() + " objId=" + objId + " oldAccess=" + current + " reason=" + reason);
			}
			
			return;
		}
		
		final int desired = entry.getAccessLevel();
		
		if (current != desired)
		{
			player.setAccessLevel(desired);
			
			LOGGER.info("GM access applied: " + player.getName() + " objId=" + objId + " oldAccess=" + current + " newAccess=" + desired + " reason=" + reason);
		}
		
		updateCharacterAccessLevel(objId, desired);
		
		if (Config.GM_STARTUP_AUTO_LIST && AdminCommandAccessRights.getInstance().hasAccess("admin_gmlist", player.getAccessLevel()))
			GmListTable.getInstance().addGm(player, false);
		else
			GmListTable.getInstance().addGm(player, true);
	}
	
	private static void enforceForOfflineObjectId(int objId, String reason)
	{
		final GMHolder entry = GmData.getInstance().getEntry(objId);
		
		final int desired;
		
		if (entry != null && entry.isEnabled() && entry.getAccessLevel() > AccessLevels.USER_ACCESS_LEVEL_NUMBER)
			desired = entry.getAccessLevel();
		else
			desired = AccessLevels.USER_ACCESS_LEVEL_NUMBER;
		
		updateCharacterAccessLevel(objId, desired);
		
		LOGGER.info("Offline GM reconcile objId=" + objId + " => access=" + desired + " reason=" + reason);
	}
	
	private static int getAccessLevelNumber(Player player)
	{
		if (player == null || player.getAccessLevel() == null)
			return AccessLevels.USER_ACCESS_LEVEL_NUMBER;
		
		return player.getAccessLevel().getLevel();
	}
	
	private static void updateCharacterAccessLevel(int objId, int lvl)
	{
		final String sql = "UPDATE characters SET accesslevel=? WHERE obj_Id=?";
		
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement(sql))
		{
			ps.setInt(1, lvl);
			ps.setInt(2, objId);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Failed update accesslevel objId=" + objId + " lvl=" + lvl, e);
		}
	}
	
	/* ================= WATCHER ================= */
	
	private void startWatcher()
	{
		try
		{
			if (!Files.exists(WATCH_DIR))
				Files.createDirectories(WATCH_DIR);
			
			_watchService = FileSystems.getDefault().newWatchService();
			
			WATCH_DIR.register(_watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
			
			_watchThread = new Thread(this::watchLoop, "gm-access-watcher");
			_watchThread.setDaemon(true);
			_watchThread.start();
			
			LOGGER.info("GM watcher started: " + WATCH_DIR.toAbsolutePath());
		}
		catch (IOException e)
		{
			LOGGER.log(Level.WARNING, "Failed to start GM watcher for " + WATCH_DIR, e);
		}
	}
	
	private void stopWatcher()
	{
		try
		{
			if (_watchService != null)
				_watchService.close();
		}
		catch (Exception ignored)
		{
		}
		
		if (_watchThread != null)
			_watchThread.interrupt();
		
		_watchThread = null;
		_watchService = null;
		
		final ScheduledFuture<?> pending = _pendingReload;
		
		if (pending != null)
			pending.cancel(false);
	}
	
	private void watchLoop()
	{
		while (true)
		{
			WatchKey key;
			
			try
			{
				key = _watchService.take();
			}
			catch (InterruptedException | ClosedWatchServiceException e)
			{
				return;
			}
			catch (Exception e)
			{
				LOGGER.log(Level.WARNING, "GM watcher loop failed.", e);
				return;
			}
			
			boolean hit = false;
			
			for (WatchEvent<?> event : key.pollEvents())
			{
				final WatchEvent.Kind<?> kind = event.kind();
				
				if (kind == StandardWatchEventKinds.OVERFLOW)
					continue;
				
				final Path changed = (Path) event.context();
				
				if (changed != null && WATCH_FILE.equalsIgnoreCase(changed.getFileName().toString()))
					hit = true;
			}
			
			final boolean valid = key.reset();
			
			if (!valid)
				return;
			
			if (hit)
				scheduleReloadDebounced();
		}
	}
	
	private void scheduleReloadDebounced()
	{
		final long now = System.currentTimeMillis();
		final long last = _lastReloadAt.get();
		
		if (now - last < RELOAD_DEBOUNCE_MS)
		{
			final ScheduledFuture<?> old = _pendingReload;
			
			if (old != null)
				old.cancel(false);
		}
		
		final ScheduledFuture<?> previous = _pendingReload;
		
		if (previous != null)
			previous.cancel(false);
		
		_pendingReload = ThreadPool.schedule(() -> {
			try
			{
				final Path file = WATCH_DIR.resolve(WATCH_FILE);
				
				if (!Files.exists(file))
				{
					LOGGER.warning("GM xml reload ignored. File not found: " + file.toAbsolutePath());
					return;
				}
				
				if (!Files.isReadable(file))
				{
					LOGGER.warning("GM xml reload ignored. File is not readable: " + file.toAbsolutePath());
					return;
				}
				
				if (Files.size(file) <= 0)
				{
					LOGGER.warning("GM xml reload ignored. File is empty: " + file.toAbsolutePath());
					return;
				}
				
				GmData.getInstance().reload();
				
				_lastReloadAt.set(System.currentTimeMillis());
				
				// Importante:
				// Reload automático não pode rebaixar GM.
				// Ele apenas reaplica accesslevel para quem estiver válido no XML.
				reapplyAllOnline("XmlReload", false);
				
				LOGGER.info("GM xml reloaded safely.");
			}
			catch (Exception e)
			{
				LOGGER.log(Level.WARNING, "GM xml reload failed. Keeping current online GM access.", e);
			}
		}, RELOAD_DELAY_MS);
	}
	
	public static GmAccessService getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final GmAccessService INSTANCE = new GmAccessService();
	}
}