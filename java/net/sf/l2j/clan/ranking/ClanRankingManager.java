package net.sf.l2j.clan.ranking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.util.Broadcast;

public class ClanRankingManager 
{	
	private final static Logger _log = Logger.getLogger(ClanRankingManager.class.getName());

	public static Map<Integer, Integer> _clanRanking = new HashMap<>();

	public ClanRankingManager()
	{
		load();
	}

	private void load() 
	{
		
	}

	public static ClanRankingManager getInstance() 
	{
		return SingletonHolder.INSTANCE;
	}

	public void addClanRanking(int _clanId, int points)
	{
		_clanRanking.put(Integer.valueOf(_clanId), Integer.valueOf(points));
	}
	
	public static void claimReward()
	{		
		if (getTop1stClanRanking() == 0 && getTop2ndClanRanking() == 0 && getTop3rdClanRanking() == 0)
			Broadcast.ServerAnnounce("Clan Ranking is finished without winners ::");
		else
		{
			if (getTop1stClanRanking() != 0)
			{
				Broadcast.ServerAnnounce("1st Clan Ranking Winner is " + getTop1stClanRankingName() + " with " + getTop1stPvpClanRankingPoints() + " point's ::");
				addReward1st(getClanRanking1stPlayerReward());
			}
			
			if (getTop2ndClanRanking() != 0)
			{
				Broadcast.ServerAnnounce("2nd Clan Ranking Winner is " + getTop2ndClanRankingName() + " with " + getTop2ndPvpClanRankingPoints() + " point's ::");
				addReward2nd(getClanRanking2ndPlayerReward());
			}

			if (getTop3rdClanRanking() != 0)
			{
				Broadcast.ServerAnnounce("3rd Clan Ranking Winner is " + getTop3rdClanRankingName() + " with " + getTop3rdPvpClanRankingPoints() + " point's ::");
				addReward3rd(getClanRanking3rdPlayerReward());
			}
		}
		Broadcast.ServerAnnounce("Next Ranking end: " + TaskClanRankingReward.getNextHourToDate() + " ::");
	}
	
//	static int getClanRanking1stPlayerReward()
//	{
//		int id = 0;
//
//		try (Connection con = ConnectionPool.getConnection())
//		{
//			PreparedStatement statement = con.prepareStatement("SELECT clan_id, champ_points, boss_points, siege_points FROM clan_points WHERE ((champ_points + boss_points + siege_points)>0) ORDER BY (champ_points + boss_points + siege_points) desc LIMIT 1");
//			ResultSet rset = statement.executeQuery();
//
//			while (rset.next())
//			{
//				id = rset.getInt("clan_id");
//				
//				PreparedStatement leaderId = con.prepareStatement("SELECT leader_id FROM clan_data WHERE clan_id=" + id);
//				ResultSet result2 = leaderId.executeQuery();
//				
//				while (result2.next())
//				{
//					id = result2.getInt("leader_id");
//				}
//				leaderId.close();
//			}
//			rset.close();
//			statement.close();
//		}
//
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//		return id;
//	}
	static int getClanRanking1stPlayerReward() {
	    int id = 0;

	    try (Connection con = ConnectionPool.getConnection()) {
	        // Usando try-with-resources para statement e resultSet
	        try (PreparedStatement statement = con.prepareStatement("SELECT clan_id, champ_points, boss_points, siege_points FROM clan_points WHERE ((champ_points + boss_points + siege_points)>0) ORDER BY (champ_points + boss_points + siege_points) desc LIMIT 1");
	             ResultSet rset = statement.executeQuery()) {
	            
	            if (rset.next()) {
	                id = rset.getInt("clan_id");
	                
	                // Usando try-with-resources para o PreparedStatement e ResultSet dentro deste bloco
	                try (PreparedStatement leaderId = con.prepareStatement("SELECT leader_id FROM clan_data WHERE clan_id=?");
	                     ResultSet result2 = leaderId.executeQuery()) {
	                     
	                    leaderId.setInt(1, id); // Definindo o parâmetro para o prepared statement
	                    if (result2.next()) {
	                        id = result2.getInt("leader_id");
	                    }
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return id;
	}

	
//	static int getClanRanking2ndPlayerReward()
//	{
//		int id = 0;
//
//		try (Connection con = ConnectionPool.getConnection())
//		{
//			PreparedStatement statement = con.prepareStatement("SELECT clan_id, champ_points, boss_points, siege_points FROM clan_points WHERE ((champ_points + boss_points + siege_points)>0) ORDER BY (champ_points + boss_points + siege_points) desc LIMIT 1, 1");
//			ResultSet rset = statement.executeQuery();
//
//			while (rset.next())
//			{
//				id = rset.getInt("clan_id");
//				
//				PreparedStatement leaderId = con.prepareStatement("SELECT leader_id FROM clan_data WHERE clan_id=" + id);
//				ResultSet result2 = leaderId.executeQuery();
//				
//				while (result2.next())
//				{
//					id = result2.getInt("leader_id");
//				}
//				leaderId.close();
//			}
//			rset.close();
//			statement.close();
//		}
//
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//		return id;
//	}
	static int getClanRanking2ndPlayerReward() {
	    int id = 0;

	    try (Connection con = ConnectionPool.getConnection()) {
	        // Usando try-with-resources para PreparedStatement e ResultSet
	        try (PreparedStatement statement = con.prepareStatement("SELECT clan_id, champ_points, boss_points, siege_points FROM clan_points WHERE ((champ_points + boss_points + siege_points)>0) ORDER BY (champ_points + boss_points + siege_points) desc LIMIT 1, 1");
	             ResultSet rset = statement.executeQuery()) {
	             
	            if (rset.next()) {
	                id = rset.getInt("clan_id");

	                // Usando try-with-resources para o PreparedStatement e ResultSet dentro deste bloco
	                try (PreparedStatement leaderId = con.prepareStatement("SELECT leader_id FROM clan_data WHERE clan_id=?");
	                     ResultSet result2 = leaderId.executeQuery()) {

	                    leaderId.setInt(1, id); // Definindo o parâmetro para o PreparedStatement
	                    if (result2.next()) {
	                        id = result2.getInt("leader_id");
	                    }
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return id;
	}

	
//	static int getClanRanking3rdPlayerReward()
//	{
//		int id = 0;
//
//		try (Connection con = ConnectionPool.getConnection())
//		{
//			PreparedStatement statement = con.prepareStatement("SELECT clan_id, champ_points, boss_points, siege_points FROM clan_points WHERE ((champ_points + boss_points + siege_points)>0) ORDER BY (champ_points + boss_points + siege_points) desc LIMIT 2, 1");
//			ResultSet rset = statement.executeQuery();
//
//			while (rset.next())
//			{
//				id = rset.getInt("clan_id");
//				
//				PreparedStatement leaderId = con.prepareStatement("SELECT leader_id FROM clan_data WHERE clan_id=" + id);
//				ResultSet result2 = leaderId.executeQuery();
//				
//				while (result2.next())
//				{
//					id = result2.getInt("leader_id");
//				}
//				leaderId.close();
//			}
//			rset.close();
//			statement.close();
//		}
//
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//		return id;
//	}
	static int getClanRanking3rdPlayerReward() {
	    int id = 0;

	    try (Connection con = ConnectionPool.getConnection()) {
	        // Usando try-with-resources para PreparedStatement e ResultSet
	        try (PreparedStatement statement = con.prepareStatement("SELECT clan_id, champ_points, boss_points, siege_points FROM clan_points WHERE ((champ_points + boss_points + siege_points)>0) ORDER BY (champ_points + boss_points + siege_points) desc LIMIT 2, 1");
	             ResultSet rset = statement.executeQuery()) {

	            if (rset.next()) {
	                id = rset.getInt("clan_id");

	                // Usando try-with-resources para o PreparedStatement e ResultSet dentro deste bloco
	                try (PreparedStatement leaderId = con.prepareStatement("SELECT leader_id FROM clan_data WHERE clan_id=?");
	                     ResultSet result2 = leaderId.executeQuery()) {

	                    leaderId.setInt(1, id); // Definindo o parâmetro para o PreparedStatement
	                    if (result2.next()) {
	                        id = result2.getInt("leader_id");
	                    }
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return id;
	}

	
	private static void addReward1st(int objId)
	{
		Player player = L2World.getInstance().getPlayer(objId);
		
		for (int[] reward : ClanRankingConfig.TOP_1ST_CLAN_REWARDS)
		{
			if (player != null && player.isOnline())
			{
				InventoryUpdate iu = new InventoryUpdate();
				
				player.addItem("Clan Reward", reward[0], reward[1], player, true);
				player.sendPacket(new ExShowScreenMessage("Congratulations " + player.getName() + " your clan are the top 1 of the week.", 3000, 0x02, true));
				player.getInventory().updateDatabase();
				player.sendPacket(iu);
			}
			else
				addOfflineItem(objId, reward[0], reward[1]);
		}
	}
	
	private static void addReward2nd(int objId)
	{
		Player player = L2World.getInstance().getPlayer(objId);
		
		for (int[] reward : ClanRankingConfig.TOP_2ND_CLAN_REWARDS)
		{
			if (player != null && player.isOnline())
			{
				InventoryUpdate iu = new InventoryUpdate();
				
				player.addItem("Clan Reward", reward[0], reward[1], player, true);
				player.sendPacket(new ExShowScreenMessage("Congratulations " + player.getName() + " your clan are the top 2 of the week.", 3000, 0x02, true));
				player.getInventory().updateDatabase();
				player.sendPacket(iu);
			}
			else
				addOfflineItem(objId, reward[0], reward[1]);
		}
	}
	
	private static void addReward3rd(int objId)
	{
		Player player = L2World.getInstance().getPlayer(objId);
		
		for (int[] reward : ClanRankingConfig.TOP_3RD_CLAN_REWARDS)
		{
			if (player != null && player.isOnline())
			{
				InventoryUpdate iu = new InventoryUpdate();
				
				player.addItem("Clan Reward", reward[0], reward[1], player, true);
				player.sendPacket(new ExShowScreenMessage("Congratulations " + player.getName() + " your clan are the top 3 of the week.", 3000, 0x02, true));
				player.getInventory().updateDatabase();
				player.sendPacket(iu);
			}
			else
				addOfflineItem(objId, reward[0], reward[1]);
		}
	}
	
	private static void addOfflineItem(int ownerId, int itemId, int count)
	{
		Item item = ItemTable.getInstance().getTemplate(itemId);
		int objectId = IdFactory.getInstance().getNextId();
		
		try (Connection con = ConnectionPool.getConnection())
		{
			if (count > 1 && !item.isStackable())
				return;
			
			PreparedStatement statement = con.prepareStatement("INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,mana_left,time) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, ownerId);
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
			e.printStackTrace();
		}
	}

	static int getTop1stPvpClanRankingPoints()
	{
		int id = 0;

		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT clan_id, champ_points, boss_points, siege_points FROM clan_points WHERE ((champ_points + boss_points + siege_points)>0) ORDER BY (champ_points + boss_points + siege_points) desc LIMIT 1");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				id = rset.getInt("champ_points") + rset.getInt("boss_points") + rset.getInt("siege_points");
			}
			rset.close();
			statement.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
		return id;
	}

	static int getTop2ndPvpClanRankingPoints()
	{
		int id = 0;

		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT clan_id, champ_points, boss_points, siege_points FROM clan_points WHERE ((champ_points + boss_points + siege_points)>0) ORDER BY (champ_points + boss_points + siege_points) desc LIMIT 1, 1");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				id = rset.getInt("champ_points") + rset.getInt("boss_points") + rset.getInt("siege_points");
			}
			rset.close();
			statement.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
		return id;
	}

	static int getTop3rdPvpClanRankingPoints()
	{
		int id = 0;

		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT clan_id, champ_points, boss_points, siege_points FROM clan_points WHERE ((champ_points + boss_points + siege_points)>0) ORDER BY (champ_points + boss_points + siege_points) desc LIMIT 2, 1");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				id = rset.getInt("champ_points") + rset.getInt("boss_points") + rset.getInt("siege_points");
			}
			rset.close();
			statement.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
		return id;
	}
	
//	static String getTop1stClanRankingName()
//	{
//		String name = null;
//
//		try (Connection con = ConnectionPool.getConnection())
//		{
//			PreparedStatement statement = con.prepareStatement("SELECT clan_id, champ_points, boss_points, siege_points FROM clan_points WHERE ((champ_points + boss_points + siege_points)>0) ORDER BY (champ_points + boss_points + siege_points) desc LIMIT 1");
//			ResultSet rset = statement.executeQuery();
//
//			while (rset.next())
//			{
//				String owner = rset.getString("clan_id");
//				
//				PreparedStatement charname = con.prepareStatement("SELECT clan_name FROM clan_data WHERE clan_id=" + owner);
//				ResultSet result2 = charname.executeQuery();
//				
//				while (result2.next())
//				{
//					name = result2.getString("clan_name");
//				}
//				charname.close();
//			}
//			rset.close();
//			statement.close();
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//
//		return name;
//	}
	static String getTop1stClanRankingName() {
	    String name = null;

	    try (Connection con = ConnectionPool.getConnection()) {
	        // Usando try-with-resources para PreparedStatement e ResultSet
	        try (PreparedStatement statement = con.prepareStatement("SELECT clan_id, champ_points, boss_points, siege_points FROM clan_points WHERE ((champ_points + boss_points + siege_points)>0) ORDER BY (champ_points + boss_points + siege_points) desc LIMIT 1");
	             ResultSet rset = statement.executeQuery()) {

	            if (rset.next()) {
	                String owner = rset.getString("clan_id");

	                // Usando try-with-resources para PreparedStatement e ResultSet dentro deste bloco
	                try (PreparedStatement charname = con.prepareStatement("SELECT clan_name FROM clan_data WHERE clan_id=?")) {
	                    charname.setString(1, owner);  // Definindo o parâmetro para o PreparedStatement
	                    try (ResultSet result2 = charname.executeQuery()) {

	                        if (result2.next()) {
	                            name = result2.getString("clan_name");
	                        }
	                    }
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return name;
	}


//	static String getTop2ndClanRankingName()
//	{
//		String name = null;
//
//		try (Connection con = ConnectionPool.getConnection())
//		{
//			PreparedStatement statement = con.prepareStatement("SELECT clan_id, champ_points, boss_points, siege_points FROM clan_points WHERE ((champ_points + boss_points + siege_points)>0) ORDER BY (champ_points + boss_points + siege_points) desc LIMIT 1, 1");
//			ResultSet rset = statement.executeQuery();
//
//			while (rset.next())
//			{
//				String owner = rset.getString("clan_id");
//				
//				PreparedStatement charname = con.prepareStatement("SELECT clan_name FROM clan_data WHERE clan_id=" + owner);
//				ResultSet result2 = charname.executeQuery();
//				
//				while (result2.next())
//				{
//					name = result2.getString("clan_name");
//				}
//				charname.close();
//			}
//			rset.close();
//			statement.close();
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//
//		return name;
//	}
	static String getTop2ndClanRankingName() {
	    String name = null;

	    try (Connection con = ConnectionPool.getConnection()) {
	        // Usando try-with-resources para PreparedStatement e ResultSet
	        try (PreparedStatement statement = con.prepareStatement("SELECT clan_id, champ_points, boss_points, siege_points FROM clan_points WHERE ((champ_points + boss_points + siege_points)>0) ORDER BY (champ_points + boss_points + siege_points) desc LIMIT 1, 1");
	             ResultSet rset = statement.executeQuery()) {

	            if (rset.next()) {
	                String owner = rset.getString("clan_id");

	                // Usando try-with-resources para PreparedStatement e ResultSet dentro deste bloco
	                try (PreparedStatement charname = con.prepareStatement("SELECT clan_name FROM clan_data WHERE clan_id=?")) {
	                    charname.setString(1, owner); // Definindo o parâmetro para o PreparedStatement
	                    try (ResultSet result2 = charname.executeQuery()) {

	                        if (result2.next()) {
	                            name = result2.getString("clan_name");
	                        }
	                    }
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return name;
	}


//	static String getTop3rdClanRankingName()
//	{
//		String name = null;
//
//		try (Connection con = ConnectionPool.getConnection())
//		{
//			PreparedStatement statement = con.prepareStatement("SELECT clan_id, champ_points, boss_points, siege_points FROM clan_points WHERE ((champ_points + boss_points + siege_points)>0) ORDER BY (champ_points + boss_points + siege_points) desc LIMIT 2, 1");
//			ResultSet rset = statement.executeQuery();
//
//			while (rset.next())
//			{
//				String owner = rset.getString("clan_id");
//				
//				PreparedStatement charname = con.prepareStatement("SELECT clan_name FROM clan_data WHERE clan_id=" + owner);
//				ResultSet result2 = charname.executeQuery();
//				
//				while (result2.next())
//				{
//					name = result2.getString("clan_name");
//				}
//				charname.close();
//			}
//			rset.close();
//			statement.close();
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//
//		return name;
//	}
	static String getTop3rdClanRankingName() {
	    String name = null;

	    try (Connection con = ConnectionPool.getConnection()) {
	        // Usando try-with-resources para PreparedStatement e ResultSet
	        try (PreparedStatement statement = con.prepareStatement("SELECT clan_id, champ_points, boss_points, siege_points FROM clan_points WHERE ((champ_points + boss_points + siege_points)>0) ORDER BY (champ_points + boss_points + siege_points) desc LIMIT 2, 1");
	             ResultSet rset = statement.executeQuery()) {

	            if (rset.next()) {
	                String owner = rset.getString("clan_id");

	                // Usando try-with-resources para PreparedStatement e ResultSet dentro deste bloco
	                try (PreparedStatement charname = con.prepareStatement("SELECT clan_name FROM clan_data WHERE clan_id=?")) {
	                    charname.setString(1, owner); // Definindo o parâmetro para o PreparedStatement
	                    try (ResultSet result2 = charname.executeQuery()) {

	                        if (result2.next()) {
	                            name = result2.getString("clan_name");
	                        }
	                    }
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return name;
	}

	
	static int getTop1stClanRanking()
	{
		int id = 0;

		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT clan_id, champ_points, boss_points, siege_points FROM clan_points WHERE ((champ_points + boss_points + siege_points)>0) ORDER BY (champ_points + boss_points + siege_points) desc LIMIT 1");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				id = rset.getInt("champ_points") + rset.getInt("boss_points") + rset.getInt("siege_points");
			}
			rset.close();
			statement.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
		return id;
	}

	static int getTop2ndClanRanking()
	{
		int id = 0;

		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT clan_id, champ_points, boss_points, siege_points FROM clan_points WHERE ((champ_points + boss_points + siege_points)>0) ORDER BY (champ_points + boss_points + siege_points) desc LIMIT 1, 1");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				id = rset.getInt("champ_points") + rset.getInt("boss_points") + rset.getInt("siege_points");
			}
			rset.close();
			statement.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
		return id;
	}

	static int getTop3rdClanRanking()
	{
		int id = 0;

		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT clan_id, champ_points, boss_points, siege_points FROM clan_points WHERE ((champ_points + boss_points + siege_points)>0) ORDER BY (champ_points + boss_points + siege_points) desc LIMIT 2, 1");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				id = rset.getInt("champ_points") + rset.getInt("boss_points") + rset.getInt("siege_points");
			}
			rset.close();
			statement.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
		return id;
	}
	
	public final static void cleanUp()
	{
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM clan_points");
			statement.executeUpdate();
			statement.close();
			_clanRanking.clear();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not clean clan points: ", e);
		}
	}
	
	public Map<Integer, Integer> getClanRanking()
	{
		return _clanRanking;
	}

	private static class SingletonHolder 
	{
		protected static final ClanRankingManager INSTANCE = new ClanRankingManager();
	}
}