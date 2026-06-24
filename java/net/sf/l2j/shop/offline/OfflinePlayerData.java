package net.sf.l2j.shop.offline;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.L2GameClient.GameClientState;

public class OfflinePlayerData
{
	private static final Logger LOGGER = Logger.getLogger(OfflinePlayerData.class.getName());

	// SQL DEFINITIONS
	private static final String SAVE_OFFLINE_STATUS = "INSERT INTO character_offline_players (`charId`,`time`,`type`,`title`) VALUES (?,?,?,?)";
	private static final String CLEAR_OFFLINE_TABLE = "DELETE FROM character_offline_players";
	private static final String LOAD_OFFLINE_STATUS = "SELECT * FROM character_offline_players";

	public void storeOffliners()
	{
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement save_offline_status = con.prepareStatement(SAVE_OFFLINE_STATUS))
		{
			try (Statement stm = con.createStatement())
			{
				stm.execute(CLEAR_OFFLINE_TABLE);
			}
			for (Player pc : L2World.getInstance().getPlayers())
			{
				try
				{
					if (pc.isOff() && pc.getClient() != null && (pc.getClient() == null || pc.getClient().isDetached()))
					{
						if (pc.isInStoreMode())
						{
							return;
						}
						
						save_offline_status.setInt(1, pc.getObjectId());
						save_offline_status.setLong(2, pc.getOfflineTesteTime());
						save_offline_status.setInt(3, pc.isOnlineInt());
						save_offline_status.setString(4, pc.getTitle());
						if (!Config.ENABLE_OFFLINE_PLAYER)
							continue;
						
						save_offline_status.execute();
					}
				}
				catch (Exception e)
				{
					LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Error while saving offline players: " + pc.getObjectId() + " " + e, e);
				}
			}

			LOGGER.info(getClass().getSimpleName() + ": Offline players stored.");
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Error while saving offline players: " + e, e);
		}
	}

	public void restoreOfflineTraders()
	{
		LOGGER.info(getClass().getSimpleName() + ": Loading offline players...");
	
		try (Connection con = ConnectionPool.getConnection(); Statement stm = con.createStatement(); ResultSet rs = stm.executeQuery(LOAD_OFFLINE_STATUS))
		{
			int nTraders = 0;
			while (rs.next())
			{
				final long time = rs.getLong("time");
				if (Config.OFFLINE_MAX_DAYS_PLAYERS > 0)
				{
					final Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(time);
					cal.add(Calendar.DAY_OF_YEAR, Config.OFFLINE_MAX_DAYS_PLAYERS);
					if (cal.getTimeInMillis() <= System.currentTimeMillis())
						continue;
				}
				final Player player = Player.restore(rs.getInt("charId"));
				if (player == null)
					continue;
				
				//if (player.isInStoreMode())
					//return;
				

				//try (PreparedStatement stm_items = con.prepareStatement(LOAD_OFFLINE_ITEMS))
				//{
					player.isRunning();
					player.setOnlineStatus(true, false);

					L2World.getInstance().addPlayer(player);

					final L2GameClient client = new L2GameClient(null);
					client.setDetached(true);
					player.setClient(client);
					//client.setPlayer(player);
					client.setActiveChar(player);
					client.setAccountName(player.getAccountNamePlayer());
					player.setOnlineStatus(true, true);
					client.setState(GameClientState.IN_GAME);
					player.setOff(true);
					player.setOfflineTesteTime(time);
					player.spawnMe();

					LoginServerThread.getInstance().addGameServerLogin(player.getAccountName(), client);

					//stm_items.setInt(1, player.getObjectId());

					if (Config.OFFLINE_PLAYER_SLEEP)
					{
						player.startAbnormalEffect(0x000080);
					}
					//player.setStoreType(type);
					player.restoreEffects();
					player.broadcastUserInfo();

					nTraders++;
				//}
				//catch (Exception e)
				//{
				//	LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Error loading players: " + player, e);

				//	player.deleteMe();
				//}
			}

			LOGGER.info(getClass().getSimpleName() + ": Loaded: " + nTraders + " offline players(s)");

			try (Statement stm1 = con.createStatement())
			{
				stm1.execute(CLEAR_OFFLINE_TABLE);
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Error while loading offline players: ", e);
		}
	}

	public boolean offlinePlayer(Player player)
	{
		if (player.isInOlympiadMode() || player.isInStoreMode() || player.isFestivalParticipant() || player.isInJail() || player.getBoat() != null)
			return false;
		
		boolean EnableSetOffline = true;

		if(Config.ENABLE_OFFLINE_PEACE_ZONE)
		{
			if(!player.isInsideZone(ZoneId.PEACE) || !player.isInsideZone(ZoneId.TOWN))
			{
				EnableSetOffline = false;
			}
		}
		return EnableSetOffline;
	}	

	public static OfflinePlayerData getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final OfflinePlayerData _instance = new OfflinePlayerData();
	}
}