package net.sf.l2j.AutoFarm;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.handler.tutorialhandlers.Autofarm;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedMenu;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.ZoneId;

public enum AutofarmManager
{
	INSTANCE;
	
	private static final Logger LOGGER = Logger.getLogger(AutofarmManager.class.getName());
	private static final long ITERATION_SPEED_MS = 500L;
	
	private final ConcurrentHashMap<Integer, AutofarmPlayerRoutine> activeFarmers = new ConcurrentHashMap<>();
	private ScheduledFuture<?> onUpdateTask = ThreadPool.scheduleAtFixedRate("AutofarmManager: onUpdate Task", onUpdate(), 1000, ITERATION_SPEED_MS);
	
	private Runnable onUpdate()
	{
		return () -> activeFarmers.forEach((objectId, routine) ->
		{
			if (routine == null || !routine.isValid())
			{
				removeRoutine(objectId, false);
				return;
			}
			
			try
			{
				routine.executeRoutine();
			}
			catch (Exception e)
			{
				LOGGER.log(Level.WARNING, "Autofarm routine failed for objectId=" + objectId + ". Routine was stopped to avoid memory/task leak.", e);
				removeRoutine(objectId, true);
			}
		});
	}
	
	public void startFarm(Player player)
	{
		if (!isValidPlayer(player))
			return;
		
		if (Config.ENABLE_COMMAND_VIP_AUTOFARM)
		{
			if (!player.isVip())
			{
				VoicedMenu.showMenuHtml(player);
				player.sendMessage("You are not VIP member.");
				return;
			}
		}
		
		if (Config.NO_USE_FARM_IN_PEACE_ZONE)
		{
			if (player.isInsideZone(ZoneId.PEACE))
			{
				VoicedMenu.showMenuHtml(player);
				player.sendMessage("No Use Auto farm in Peace Zone.");
				return;
			}
		}
		
		final int objectId = player.getObjectId();
		final AutofarmPlayerRoutine previous = activeFarmers.remove(objectId);
		if (previous != null)
			previous.stop();
		
		player.setAutoFarm(true);
		activeFarmers.put(objectId, new AutofarmPlayerRoutine(player));
		
		player.sendMessage("Autofarming Activated.");
		Autofarm.showAutoFarm(player);
		player.broadcastUserInfo();
	}
	
	public void stopFarm(Player player)
	{
		stopFarm(player, true);
	}
	
	private void stopFarm(Player player, boolean notify)
	{
		if (player == null)
			return;
		
		final AutofarmPlayerRoutine routine = activeFarmers.remove(player.getObjectId());
		if (routine != null)
			routine.stop();
		
		player.setAutoFarm(false);
		
		if (notify && player.isOnline())
		{
			player.sendMessage("Autofarming Disabled.");
			Autofarm.showAutoFarm(player);
			player.broadcastUserInfo();
		}
	}
	
	private void removeRoutine(int objectId, boolean updatePlayerFlag)
	{
		final AutofarmPlayerRoutine routine = activeFarmers.remove(objectId);
		if (routine == null)
			return;
		
		final Player player = routine.getPlayer();
		routine.stop();
		
		if (updatePlayerFlag && player != null)
		{
			player.setAutoFarm(false);
			if (player.isOnline())
			{
				Autofarm.showAutoFarm(player);
				player.broadcastUserInfo();
			}
		}
	}
	
	public synchronized void stopFarmTask()
	{
		if (onUpdateTask != null)
		{
			onUpdateTask.cancel(false);
			onUpdateTask = null;
		}
		
		activeFarmers.forEach((objectId, routine) ->
		{
			if (routine != null)
				routine.stop();
		});
		activeFarmers.clear();
	}
	
	public void toggleFarm(Player player)
	{
		if (isAutofarming(player))
		{
			stopFarm(player);
			return;
		}
		
		startFarm(player);
	}
	
	public Boolean isAutofarming(Player player)
	{
		return player != null && activeFarmers.containsKey(player.getObjectId());
	}
	
	public int getActiveFarmersCount()
	{
		return activeFarmers.size();
	}
	
	public void onPlayerLogout(Player player)
	{
		stopFarm(player, false);
	}
	
	public void onDeath(Player player)
	{
		if (player == null)
			return;
		
		stopFarm(player, false);
		
		if (player.isOnline())
		{
			Autofarm.showAutoFarm(player);
			player.broadcastUserInfo();
		}
	}
	
	private static boolean isValidPlayer(Player player)
	{
		return player != null && player.isOnline();
	}
}
