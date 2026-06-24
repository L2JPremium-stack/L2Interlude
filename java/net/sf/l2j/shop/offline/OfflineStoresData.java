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
import net.sf.l2j.gameserver.model.L2ManufactureItem;
import net.sf.l2j.gameserver.model.L2ManufactureList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Player.StoreType;
import net.sf.l2j.gameserver.model.tradelist.TradeItem;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.L2GameClient.GameClientState;

public class OfflineStoresData
{
	private static final Logger LOGGER = Logger.getLogger(OfflineStoresData.class.getName());
	
	// SQL DEFINITIONS
	private static final String SAVE_OFFLINE_STATUS = "INSERT INTO character_offline_trade (`charId`,`time`,`type`,`title`) VALUES (?,?,?,?)";
	private static final String SAVE_ITEMS = "INSERT INTO character_offline_trade_items (`charId`,`item`,`count`,`price`,`enchant`) VALUES (?,?,?,?,?)";
	private static final String CLEAR_OFFLINE_TABLE = "DELETE FROM character_offline_trade";
	private static final String CLEAR_OFFLINE_TABLE_ITEMS = "DELETE FROM character_offline_trade_items";
	private static final String LOAD_OFFLINE_STATUS = "SELECT * FROM character_offline_trade";
	private static final String LOAD_OFFLINE_ITEMS = "SELECT * FROM character_offline_trade_items WHERE charId = ?";
	
	public void storeOffliners()
	{
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement save_offline_status = con.prepareStatement(SAVE_OFFLINE_STATUS); PreparedStatement save_items = con.prepareStatement(SAVE_ITEMS))
		{
			
			try (Statement stm = con.createStatement())
			{
				stm.execute(CLEAR_OFFLINE_TABLE);
				stm.execute(CLEAR_OFFLINE_TABLE_ITEMS);
			}
			
			for (Player pc : L2World.getInstance().getPlayers())
			{
				try
				{
					if (pc == null)
						continue;
					
					if (pc.getStoreType() != StoreType.NONE && (pc.getClient() == null || pc.getClient().isDetached()))
					{
						save_offline_status.setInt(1, pc.getObjectId());
						save_offline_status.setLong(2, pc.getOfflineStartTime());
						save_offline_status.setInt(3, pc.getStoreType().getId());
						
						switch (pc.getStoreType())
						{
							case BUY:
								if (!Config.OFFLINE_TRADE_ENABLE)
									continue;
								
								save_offline_status.setString(4, pc.getBuyList().getTitle());
								for (TradeItem i : pc.getBuyList().getItems())
								{
									save_items.setInt(1, pc.getObjectId());
									save_items.setInt(2, i.getItem().getItemId());
									save_items.setLong(3, i.getCount());
									save_items.setLong(4, i.getPrice());
									save_items.setLong(5, i.getEnchant());
									save_items.addBatch();
								}
								break;
							
							case SELL:
							case PACKAGE_SELL:
								if (!Config.OFFLINE_TRADE_ENABLE)
									continue;
								
								save_offline_status.setString(4, pc.getSellList().getTitle());
								pc.getSellList().updateItems();
								for (TradeItem i : pc.getSellList().getItems())
								{
									save_items.setInt(1, pc.getObjectId());
									save_items.setInt(2, i.getObjectId());
									save_items.setLong(3, i.getCount());
									save_items.setLong(4, i.getPrice());
									save_items.setLong(5, i.getEnchant());
									save_items.addBatch();
								}
								break;
							
							case MANUFACTURE:
								if (!Config.OFFLINE_CRAFT_ENABLE)
									continue;
								
								save_offline_status.setString(4, pc.getCreateList().getStoreName());
								for (L2ManufactureItem i : pc.getCreateList().getList())
								{
									save_items.setInt(1, pc.getObjectId());
									save_items.setInt(2, i.getRecipeId());
									save_items.setLong(3, 0L);
									save_items.setLong(4, i.getCost());
									save_items.setLong(5, 0L);
									save_items.addBatch();
								}
								break;
							
							case STORE_SELLBUFF:
								if (!Config.OFFLINE_SELLBUFF_ENABLED)
									continue;
								
								save_offline_status.setString(4, ""); // Título vazio ou pode ser algum valor
								save_items.setInt(1, pc.getObjectId());
								save_items.setInt(2, 0); // Não há item específico para buffs
								save_items.setLong(3, 0); // Sem contagem de itens
								save_items.setLong(4, pc.getBuffPrice()); // Preço do buff
								save_items.setLong(5, pc.isSellBuff() ? 1 : 0); // 1 se for buffer, 0 caso contrário
								save_items.addBatch();
								break;
							
							default:
								break;
						}
						
						save_items.executeBatch();
						save_items.clearBatch();
						save_offline_status.execute();
						save_offline_status.clearParameters();
					}
				}
				catch (Exception e)
				{
					LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Error while saving offline trader: " + pc.getObjectId() + " " + e, e);
				}
			}
			
			LOGGER.info(getClass().getSimpleName() + ": Offline traders stored.");
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Error while saving offline traders: " + e, e);
		}
	}
	
	public void restoreOfflineTraders()
	{
		LOGGER.info(getClass().getSimpleName() + ": Loading offline traders...");
		try (Connection con = ConnectionPool.getConnection(); Statement stm = con.createStatement(); ResultSet rs = stm.executeQuery(LOAD_OFFLINE_STATUS))
		{
			int nTraders = 0;
			while (rs.next())
			{
				final long time = rs.getLong("time");
				if (Config.OFFLINE_MAX_DAYS > 0)
				{
					final Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(time);
					cal.add(Calendar.DAY_OF_YEAR, Config.OFFLINE_MAX_DAYS);
					if (cal.getTimeInMillis() <= System.currentTimeMillis())
						continue;
				}
				
				StoreType type = null;
				for (StoreType t : StoreType.values())
				{
					if (t.getId() == rs.getInt("type"))
					{
						type = t;
						break;
					}
				}
				if (type == null)
				{
					LOGGER.warning(getClass().getSimpleName() + ": PrivateStoreType with id " + rs.getInt("type") + " could not be found.");
					continue;
				}
				if (type == StoreType.NONE)
					continue;
				
				final Player player = Player.restore(rs.getInt("charId"));
				if (player == null)
					continue;
				
				try (PreparedStatement stm_items = con.prepareStatement(LOAD_OFFLINE_ITEMS))
				{
					player.isRunning();
					player.sitDown(true);
					player.setOnlineStatus(true, false);
					
					L2World.getInstance().addPlayer(player);
					
					final L2GameClient client = new L2GameClient(null);
					client.setDetached(true);
					player.setClient(client);
					client.setActiveChar(player);
					client.setAccountName(player.getAccountNamePlayer());
					player.setOnlineStatus(true, true);
					client.setState(GameClientState.IN_GAME);
					player.setOffShop(true);
					player.setOfflineStartTime(time);
					player.spawnMe();
					
					LoginServerThread.getInstance().addGameServerLogin(player.getAccountName(), client);
					
					stm_items.setInt(1, player.getObjectId());
					try (ResultSet items = stm_items.executeQuery())
					{
						switch (type)
						{
							case BUY:
								while (items.next())
								{
									player.getBuyList().addItemByItemId(items.getInt(2), items.getInt(3), items.getInt(4));
								}
								player.getBuyList().setTitle(rs.getString("title"));
								break;
							
							case SELL:
							case PACKAGE_SELL:
								while (items.next())
									if (player.getSellList().addItem(items.getInt(2), items.getInt(3), items.getInt(4)) == null)
										throw new NullPointerException("NPE at SELL of offlineShop " + player.getObjectId() + " " + items.getInt(2) + " " + items.getInt(3) + " " + items.getInt(4));
									
								player.getSellList().setTitle(rs.getString("title"));
								player.getSellList().setPackaged(type == StoreType.PACKAGE_SELL);
								break;
							
							case MANUFACTURE:
								final L2ManufactureList createList = new L2ManufactureList();
								createList.setStoreName(rs.getString("title"));
								while (items.next())
									player.setCreateList(createList);
								player.getCreateList().setStoreName(rs.getString("title"));
								break;
							
							case STORE_SELLBUFF:
								if (!Config.OFFLINE_SELLBUFF_ENABLED)
									break;
								
								while (items.next())
								{
									// Restaurar o preço do buff
									player.getSellBuffMsg().startBuffStore(player, items.getInt(4), false);
									break; // Deixa apenas um buff, já que um jogador só pode ter um "offline buffer"
								}
								break;
							
							default:
								break;
						}
					}
					
					if (Config.OFFLINE_SET_SLEEP)
						player.startAbnormalEffect(0x000080);
					
					player.setStoreType(type);
					player.restoreEffects();
					player.broadcastUserInfo();
					
					nTraders++;
				}
				catch (Exception e)
				{
					LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Error loading trader: " + player, e);
					player.deleteMe();
				}
			}
			
			LOGGER.info(getClass().getSimpleName() + ": Loaded: " + nTraders + " offline trader(s)");
			
			try (Statement stm1 = con.createStatement())
			{
				stm1.execute(CLEAR_OFFLINE_TABLE);
				stm1.execute(CLEAR_OFFLINE_TABLE_ITEMS);
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Error while loading offline traders: ", e);
		}
	}
	
	public static boolean offlineMode(Player player)
	{
		if (player.isInOlympiadMode() || player.isFestivalParticipant() || player.isInJail() || player.getBoat() != null)
			return false;
		
		boolean canSetShop = false;
		switch (player.getStoreType())
		{
			case SELL:
			case PACKAGE_SELL:
			case BUY:
				canSetShop = Config.OFFLINE_TRADE_ENABLE;
				break;
			case MANUFACTURE:
				canSetShop = Config.OFFLINE_CRAFT_ENABLE;
				break;
			default:
				break;
		}
		if (Config.OFFLINE_MODE_IN_PEACE_ZONE && !player.isInsideZone(ZoneId.PEACE))
			canSetShop = false;
		
		return canSetShop;
	}
	
	public static OfflineStoresData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final OfflineStoresData _instance = new OfflineStoresData();
	}
}