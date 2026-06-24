package net.sf.l2j.solofarm.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.l2j.gameserver.ConnectionPool;

public class SoloFarmDAO
{
	private static final String SELECT_BALANCE = "SELECT monster_balance FROM solo_farm_player_data WHERE object_id=?";
	private static final String INSERT_DEFAULT = "INSERT IGNORE INTO solo_farm_player_data (object_id, monster_balance, total_bought, total_killed, last_enter_time) VALUES (?, 0, 0, 0, 0)";
	private static final String ADD_BALANCE = "UPDATE solo_farm_player_data SET monster_balance = monster_balance + ?, total_bought = total_bought + ? WHERE object_id=?";
	private static final String REMOVE_ONE = "UPDATE solo_farm_player_data SET monster_balance = monster_balance - 1, total_killed = total_killed + 1 WHERE object_id=? AND monster_balance > 0";
	private static final String UPDATE_ENTER = "UPDATE solo_farm_player_data SET last_enter_time=? WHERE object_id=?";
	
	public static void createIfMissing(int objectId)
	{
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement(INSERT_DEFAULT))
		{
			ps.setInt(1, objectId);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static int getBalance(int objectId)
	{
		createIfMissing(objectId);
		
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement(SELECT_BALANCE))
		{
			ps.setInt(1, objectId);
			
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
					return rs.getInt("monster_balance");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return 0;
	}
	
	public static void addBalance(int objectId, int amount)
	{
		createIfMissing(objectId);
		
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement(ADD_BALANCE))
		{
			ps.setInt(1, amount);
			ps.setInt(2, amount);
			ps.setInt(3, objectId);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static boolean consumeOneKill(int objectId)
	{
		createIfMissing(objectId);
		
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement(REMOVE_ONE))
		{
			ps.setInt(1, objectId);
			return ps.executeUpdate() > 0;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public static void updateLastEnterTime(int objectId, long time)
	{
		createIfMissing(objectId);
		
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE_ENTER))
		{
			ps.setLong(1, time);
			ps.setInt(2, objectId);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static int[] getStats(int objectId)
	{
		createIfMissing(objectId);
		
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT monster_balance, total_bought, total_killed FROM solo_farm_player_data WHERE object_id=?"))
		{
			ps.setInt(1, objectId);
			
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					return new int[]
					{
						rs.getInt("monster_balance"),
						rs.getInt("total_bought"),
						rs.getInt("total_killed")
					};
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return new int[]
		{
			0,
			0,
			0
		};
	}
}