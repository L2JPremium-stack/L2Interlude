package net.sf.l2j.gameserver.instancemanager.custom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.CharSelectInfoPackage;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.PcPolymorph;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;

public final class CharacterKillingManager
{
	private static final Logger _log = Logger.getLogger(CharacterKillingManager.class.getName());
	private int _cycle = 0;
	private long _cycleStart = 0L;
	private int _winnerPvPKills;
	private int _winnerPvPKillsCount;
	private int _winnerPKKills;
	private int _winnerPKKillsCount;
	
	private volatile CharSelectInfoPackage _winnerPvPKillsInfo;
	private volatile CharSelectInfoPackage _winnerPKKillsInfo;
	
	private ScheduledFuture<?> _scheduledKillingCycleTask = null;
	
	private final List<PcPolymorph> pvpMorphListeners = new CopyOnWriteArrayList<>();
	private final List<PcPolymorph> pkMorphListeners = new CopyOnWriteArrayList<>();
	
	public synchronized void init()
	{
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement st = con.prepareStatement("SELECT cycle, cycle_start, winner_pvpkills, winner_pvpkills_count, winner_pkkills, winner_pkkills_count FROM character_kills_info ORDER BY cycle_start DESC LIMIT 1");
			ResultSet rs = st.executeQuery())
			{
			if (rs.next())
			{
				_cycle = rs.getInt("cycle");
				_cycleStart = rs.getLong("cycle_start");
				_winnerPvPKills = rs.getInt("winner_pvpkills");
				_winnerPvPKillsCount = rs.getInt("winner_pvpkills_count");
				_winnerPKKills = rs.getInt("winner_pkkills");
				_winnerPKKillsCount = rs.getInt("winner_pkkills_count");
			}
			}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not load characters killing cycle: " + e.getMessage(), e);
		}
		
		broadcastMorphUpdate();
		
		if (_scheduledKillingCycleTask != null)
			_scheduledKillingCycleTask.cancel(true);
		long millisToNextCycle = _cycleStart + Config.CKM_CYCLE_LENGTH - System.currentTimeMillis();
		_scheduledKillingCycleTask = ThreadPool.schedule(new CharacterKillingCycleTask(), millisToNextCycle);
		
		_log.info(getClass().getSimpleName() + ": Started! Cycle: " + _cycle + " - Next cycle in: " + _scheduledKillingCycleTask.getDelay(TimeUnit.SECONDS) + "s");
	}
	
	public synchronized void newKillingCycle()
	{
		_cycleStart = System.currentTimeMillis();
		computateCyclePvPWinner();
		computateCyclePKWinner();
		refreshKillingSnapshot();
		
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement st = con.prepareStatement("INSERT INTO character_kills_info (cycle_start, winner_pvpkills, winner_pvpkills_count, winner_pkkills, winner_pkkills_count) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS))
			{
			st.setLong(1, _cycleStart);
			st.setInt(2, _winnerPvPKills);
			st.setInt(3, _winnerPvPKillsCount);
			st.setInt(4, _winnerPKKills);
			st.setInt(5, _winnerPKKillsCount);
			st.execute();
			
			try (ResultSet rs = st.getGeneratedKeys())
			{
				if (rs.next())
					_cycle = rs.getInt(1);
			}
			}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not create characters killing cycle: " + e.getMessage(), e);
		}
		
		broadcastMorphUpdate();
		
		if (_scheduledKillingCycleTask != null)
			_scheduledKillingCycleTask.cancel(true);
		_scheduledKillingCycleTask = ThreadPool.schedule(new CharacterKillingCycleTask(), Config.CKM_CYCLE_LENGTH);
	}
	
	private void computateCyclePvPWinner()
	{
		_winnerPvPKills = 0;
		_winnerPvPKillsCount = 0;
		_winnerPvPKillsInfo = null;
		
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement st = con.prepareStatement("SELECT c.obj_Id, (c.pvpkills - COALESCE(ck.pvpkills, 0)) pvpkills FROM characters c LEFT JOIN character_kills_snapshot ck ON ck.charId = c.obj_Id WHERE accesslevel = 0 ORDER BY pvpkills DESC LIMIT 1");
			ResultSet rs = st.executeQuery();)
			{
			if (rs.next())
			{
				int kills = rs.getInt(2);
				if (kills > 0)
				{
					_winnerPvPKills = rs.getInt(1);
					_winnerPvPKillsCount = kills;
				}
				
				if (_winnerPvPKills == 0)
					addReward(_winnerPvPKills, true);
				else
					addReward(_winnerPvPKills, false);
			}
			}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not computate characters killing cycle winners: " + e.getMessage(), e);
		}
	}
	
	private void computateCyclePKWinner()
	{
		_winnerPKKills = 0;
		_winnerPKKillsCount = 0;
		_winnerPKKillsInfo = null;
		
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement st = con.prepareStatement("SELECT c.obj_Id, (c.pkkills - COALESCE(ck.pkkills, 0)) pkkills FROM characters c LEFT JOIN character_kills_snapshot ck ON ck.charId = c.obj_Id WHERE accesslevel = 0 ORDER BY pkkills DESC LIMIT 1");
			ResultSet rs = st.executeQuery();)
			{
			if (rs.next())
			{
				int kills = rs.getInt(2);
				if (kills > 0)
				{
					_winnerPKKills = rs.getInt(1);
					_winnerPKKillsCount = kills;
				}
				if (_winnerPKKills == 0)
					addReward(_winnerPKKills, true);
				else
					addReward(_winnerPKKills, false);
			}
			}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not computate characters killing cycle winners: " + e.getMessage(), e);
		}
	}
	
	private static void refreshKillingSnapshot()
	{
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement stTruncate = con.prepareStatement("TRUNCATE TABLE character_kills_snapshot");
			PreparedStatement stRefresh = con.prepareStatement("INSERT INTO character_kills_snapshot (charId, pvpkills, pkkills) SELECT obj_Id, pvpkills, pkkills FROM characters WHERE (pvpkills > 0 OR pkkills > 0) AND accesslevel = 0"))
			{
			stTruncate.executeUpdate();
			stRefresh.executeUpdate();
			}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not refresh characters killing snapshot: " + e.getMessage(), e);
		}
	}
	
	public void broadcastMorphUpdate()
	{
		final CharSelectInfoPackage winnerPvPKillsInfo = getWinnerPvPKillsInfo();
		for (PcPolymorph npc : pvpMorphListeners)
			broadcastPvPMorphUpdate(npc, winnerPvPKillsInfo);
		
		final CharSelectInfoPackage winnerPKKillsInfo = getWinnerPKKillsInfo();
		for (PcPolymorph npc : pkMorphListeners)
			broadcastPKMorphUpdate(npc, winnerPKKillsInfo);
	}
	
	private void broadcastPvPMorphUpdate(PcPolymorph npc, CharSelectInfoPackage winnerPvPKillsInfo)
	{
		if (winnerPvPKillsInfo == null)
		{
			npc.setPolymorphInfo(null);
			return;
		}
		
		npc.setVisibleTitle(Config.CKM_PVP_NPC_TITLE.replaceAll("%kills%", String.valueOf(_winnerPvPKillsCount)));
		npc.setTitleColor(Config.CKM_PVP_NPC_TITLE_COLOR);
		npc.setNameColor(Config.CKM_PVP_NPC_NAME_COLOR);
		npc.setPolymorphInfo(winnerPvPKillsInfo);
		npc.broadcastPacket(new SocialAction(npc, 16));
	}
	
	private void broadcastPKMorphUpdate(PcPolymorph npc, CharSelectInfoPackage winnerPKKillsInfo)
	{
		if (winnerPKKillsInfo == null)
		{
			npc.setPolymorphInfo(null);
			return;
		}
		
		npc.setVisibleTitle(Config.CKM_PK_NPC_TITLE.replaceAll("%kills%", String.valueOf(_winnerPKKillsCount)));
		npc.setTitleColor(Config.CKM_PK_NPC_TITLE_COLOR);
		npc.setNameColor(Config.CKM_PK_NPC_NAME_COLOR);
		npc.setPolymorphInfo(winnerPKKillsInfo);
		npc.broadcastPacket(new SocialAction(npc, 16));
	}
	
	public boolean addPvPMorphListener(PcPolymorph npc)
	{
		if (npc == null)
			return false;
		
		broadcastPvPMorphUpdate(npc, getWinnerPvPKillsInfo());
		return pvpMorphListeners.add(npc);
	}
	
	public boolean removePvPMorphListener(PcPolymorph npc)
	{
		return pvpMorphListeners.remove(npc);
	}
	
	public boolean addPKMorphListener(PcPolymorph npc)
	{
		if (npc == null)
			return false;
		
		broadcastPKMorphUpdate(npc, getWinnerPKKillsInfo());
		return pkMorphListeners.add(npc);
	}
	
	public boolean removePKMorphListener(PcPolymorph npc)
	{
		return pkMorphListeners.remove(npc);
	}
	
	private CharSelectInfoPackage getWinnerPvPKillsInfo()
	{
		if (_winnerPvPKills != 0 && _winnerPvPKillsInfo == null)
			synchronized (this)
			{
				if (_winnerPvPKillsInfo == null)
					_winnerPvPKillsInfo = PcPolymorph.loadCharInfo(_winnerPvPKills);
			}
		return _winnerPvPKillsInfo;
	}
	
	private CharSelectInfoPackage getWinnerPKKillsInfo()
	{
		if (_winnerPKKills != 0 && _winnerPKKillsInfo == null)
			synchronized (this)
			{
				if (_winnerPKKillsInfo == null)
					_winnerPKKillsInfo = PcPolymorph.loadCharInfo(_winnerPKKills);
			}
		return _winnerPKKillsInfo;
	}
	
	public static class CharacterKillingCycleTask implements Runnable
	{
		@Override
		public void run()
		{
			CharacterKillingManager.getInstance().newKillingCycle();
		}
	}
	
	public static CharacterKillingManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CharacterKillingManager _instance = new CharacterKillingManager();
	}
	
	private static void addReward(int obj_id, boolean duple)
	{
		Player player = L2World.getInstance().getPlayer(obj_id);
		
		for (int[] reward : Config.CKM_PLAYER_REWARDS)
			if (player != null && player.isOnline())
			{
				InventoryUpdate iu = new InventoryUpdate();
				final Item item = ItemTable.getInstance().getTemplate(reward[0]);
				player.getInventory().addItem("Top in 24 hours", reward[0], duple ? reward[1] * 2 : reward[1], player, null);
				player.sendPacket(new ExShowScreenMessage("Congratulations " + player.getName() + " you are the player who killed more in 24 Hours.", 3000, 0x02, true));
				player.sendMessage("Top 24 Hours: You won " + item.getName() + ".");
				player.getInventory().updateDatabase();
				player.sendPacket(iu);
			}
			else
				addOfflineItem(obj_id, reward[0], duple ? reward[1] * 2 : reward[1]);
	}
	
	private static void addOfflineItem(int owner_id, int item_id, int count)
	{
		Item item = ItemTable.getInstance().getTemplate(item_id);
		int objectId = IdFactory.getInstance().getNextId();
		
		try (Connection con = ConnectionPool.getConnection())
		{
			if (count > 1 && !item.isStackable())
				return;
			
			PreparedStatement statement = con.prepareStatement("INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,mana_left,time) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, owner_id);
			statement.setInt(2, item.getItemId());
			statement.setInt(3, count);
			statement.setString(4, "INVENTORY");
			statement.setInt(5, 0);
			statement.setInt(6, 0);
			statement.setInt(7, objectId);
			statement.setInt(8, 0);
			statement.setInt(9, 0);
			statement.setInt(10, -1);
			statement.setLong(11, 0);
			statement.executeUpdate();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.severe("Could not update item char: " + e);
		}
	}
}