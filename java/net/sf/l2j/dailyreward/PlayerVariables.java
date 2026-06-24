package net.sf.l2j.dailyreward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.model.actor.Player;

public class PlayerVariables
{
	// When var exist
	public static void changeValue(Player player, String name, String value)
	{
	    if (!player.getVariables().containsKey(name))
	    {
	        player.sendMessage("Variable does not exist.");
	        return;
	    }

	    var varObject = getVarObject(player, name);
	    if (varObject == null)
	    {
	        player.sendMessage("Variable object is null.");
	        return;
	    }

	    varObject.setValue(value);

	    try (Connection con = ConnectionPool.getConnection();
	         PreparedStatement stmt = con.prepareStatement("UPDATE character_memo_alt SET value=? WHERE obj_id=? AND name=?"))
	    {
	        stmt.setString(1, value);
	        stmt.setInt(2, player.getObjectId());
	        stmt.setString(3, name);
	        stmt.executeUpdate();
	    }
	    catch (SQLException e)
	    {
	        e.printStackTrace();
	        player.sendMessage("Failed to update variable in database.");
	    }
	}

	
	public static void setVar(Player player, String name, String value, long expirationTime)
	{
	    PlayerVar existingVar = player.getVariables().get(name);
	    if (existingVar != null)
	    {
	        existingVar.stopExpireTask();
	    }

	    PlayerVar newVar = new PlayerVar(player, name, value, expirationTime);
	    player.getVariables().put(name, newVar);

	    try (Connection con = ConnectionPool.getConnection();
	         PreparedStatement stmt = con.prepareStatement("REPLACE INTO character_memo_alt (obj_id, name, value, expire_time) VALUES (?, ?, ?, ?)"))
	    {
	        stmt.setInt(1, player.getObjectId());
	        stmt.setString(2, name);
	        stmt.setString(3, value);
	        stmt.setLong(4, expirationTime);
	        stmt.executeUpdate();
	    }
	    catch (SQLException e)
	    {
	        e.printStackTrace();
	    }
	}

	
	public static void setVar(int objId, String name, String value, long expirationTime) {
	    String sql = "REPLACE INTO character_memo_alt (obj_id, name, value, expire_time) VALUES (?, ?, ?, ?)";

	    try (Connection con = ConnectionPool.getConnection();
	         PreparedStatement stmt = con.prepareStatement(sql)) {
	        stmt.setInt(1, objId);
	        stmt.setString(2, name);
	        stmt.setString(3, value);
	        stmt.setLong(4, expirationTime);
	        stmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	        // Aqui pode adicionar log ou tratamento de erro mais elaborado
	    }
	}

	
	public static void setVar(Player player, String name, int value, long expirationTime)
	{
		setVar(player, name, String.valueOf(value), expirationTime);
	}
	
	public void setVar(Player player, String name, long value, long expirationTime)
	{
		setVar(player, name, String.valueOf(value), expirationTime);
	}
	
	public static PlayerVar getVarObject(Player player, String name)
	{
		if (player.getVariables() == null)
			return null;
		
		return player.getVariables().get(name);
	}
	
	public static long getVarTimeToExpire(Player player, String name)
	{
		try
		{
			return getVarObject(player, name).getTimeToExpire();
		}
		catch (NullPointerException npe)
		{
		}
		
		return 0;
	}
	
	public static void unsetVar(Player player, String name) {
	    if (name == null || player == null)
	        return;

	    PlayerVar pv = player.getVariables().remove(name);

	    if (pv != null) {
	        if (name.contains("delete_temp_item"))
	            pv.getOwner().deleteTempItem(Integer.parseInt(pv.getValue()));
	        else if (name.contains("solo_hero")) {
	            pv.getOwner().broadcastCharInfo();
	            pv.getOwner().broadcastUserInfo();
	        }

	        // Faz a remoção no banco com try-with-resources
	        String sql = "DELETE FROM character_memo_alt WHERE obj_id=? AND name=? LIMIT 1";

	        try (Connection con = ConnectionPool.getConnection();
	             PreparedStatement stmt = con.prepareStatement(sql)) {
	            stmt.setInt(1, pv.getOwner().getObjectId());
	            stmt.setString(2, name);
	            stmt.executeUpdate();
	        } catch (SQLException e) {
	            e.printStackTrace();
	            // Pode adicionar log aqui
	        }

	        pv.stopExpireTask();
	    }
	}

	
	public static void deleteExpiredVar(Player player, String name, String value) {
	    if (name == null || player == null)
	        return;

	    if (name.contains("delete_temp_item")) {
	        player.deleteTempItem(Integer.parseInt(value));
	    }
	    /*
	     * else if(name.contains("solo_hero")) // Useless player.broadcastCharInfo();
	     */

	    String sql = "DELETE FROM character_memo_alt WHERE obj_id=? AND name=? LIMIT 1";

	    try (Connection con = ConnectionPool.getConnection();
	         PreparedStatement stmt = con.prepareStatement(sql)) {
	        stmt.setInt(1, player.getObjectId());
	        stmt.setString(2, name);
	        stmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	        // Pode adicionar log se quiser
	    }
	}

	
	public static String getVar(Player player, String name)
	{
		PlayerVar pv = getVarObject(player, name);
		
		if (pv == null)
			return null;
		
		return pv.getValue();
	}
	
//	public static long getVarTimeToExpireSQL(L2PcInstance player, String name)
//	{
//		long expireTime = 0;
//		try (Connection con = ConnectionPool.getConnection())
//		{
//			PreparedStatement statement = con.prepareStatement("SELECT expire_time FROM character_memo_alt WHERE obj_id = ? AND name = ?");
//			statement.setLong(1, player.getObjectId());
//			statement.setString(2, name);
//			for (ResultSet rset = statement.executeQuery(); rset.next();)
//				expireTime = rset.getLong("expire_time");
//			
//			con.close();
//			statement.close();
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//		
//		return expireTime;
//	}
	public static long getVarTimeToExpireSQL(Player player, String name)
	{
	    long expireTime = 0;
	    try (Connection con = ConnectionPool.getConnection();
	         PreparedStatement statement = con.prepareStatement("SELECT expire_time FROM character_memo_alt WHERE obj_id = ? AND name = ?"))
	    {
	        statement.setLong(1, player.getObjectId());
	        statement.setString(2, name);

	        // Try-with-resources for ResultSet to ensure it's closed automatically
	        try (ResultSet rset = statement.executeQuery())
	        {
	            while (rset.next())
	            {
	                expireTime = rset.getLong("expire_time");
	            }
	        }
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }

	    return expireTime;
	}

	
	public static boolean getVarB(Player player, String name, boolean defaultVal)
	{
		PlayerVar pv = getVarObject(player, name);
		
		if (pv == null)
			return defaultVal;
		
		return pv.getValueBoolean();
	}
	
	public static boolean getVarB(Player player, String name)
	{
		return getVarB(player, name, false);
	}
	
	public long getVarLong(Player player, String name)
	{
		return getVarLong(player, name, 0L);
	}
	
	public long getVarLong(Player player, String name, long defaultVal)
	{
		long result = defaultVal;
		String var = getVar(player, name);
		if (var != null)
			result = Long.parseLong(var);
		
		return result;
	}
	
	public static int getVarInt(Player player, String name)
	{
		return getVarInt(player, name, 0);
	}
	
	public static int getVarInt(Player player, String name, int defaultVal)
	{
		int result = defaultVal;
		String var = getVar(player, name);
		if (var != null)
		{
			if (var.equalsIgnoreCase("true"))
				result = 1;
			else if (var.equalsIgnoreCase("false"))
				result = 0;
			else
				result = Integer.parseInt(var);
		}
		return result;
	}
	
	public static void votedResult(Player player) {
	    String sql = "SELECT * FROM character_memo_alt WHERE obj_id=? AND name=?";
	    try (Connection con = ConnectionPool.getConnection();
	         PreparedStatement offline = con.prepareStatement(sql)) {
	         
	        offline.setInt(1, player.getObjectId());
	        offline.setString(2, "voted"); // Supondo que 'name' seja "voted", pois faltava o parâmetro no seu código
	        
	        try (ResultSet rs = offline.executeQuery()) {
	            if (!rs.next()) {
	                insertVoteSites(player);
	            }
	        }
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	
	public static void insertVoteSites(Player player) {
	    String sql = "INSERT INTO character_memo_alt (obj_id, name, value, expire_time) VALUES (?, ?, ?, ?)";
	    try (Connection con = ConnectionPool.getConnection();
	         PreparedStatement stmt = con.prepareStatement(sql)) {
	         
	        stmt.setInt(1, player.getObjectId());
	        stmt.setString(2, "votedSites");
	        stmt.setString(3, "0");
	        stmt.setLong(4, 0);
	        stmt.execute();
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	
	public static void loadVariables(Player player) {
	    String sql = "SELECT * FROM character_memo_alt WHERE obj_id = ?";
	    try (Connection con = ConnectionPool.getConnection();
	         PreparedStatement stmt = con.prepareStatement(sql)) {
	        
	        stmt.setInt(1, player.getObjectId());
	        try (ResultSet rs = stmt.executeQuery()) {
	            long curtime = System.currentTimeMillis();
	            while (rs.next()) {
	                String name = rs.getString("name");
	                String value = rs.getString("value");
	                long expire_time = rs.getLong("expire_time");

	                if ((expire_time <= curtime) && (expire_time > 0)) {
	                    deleteExpiredVar(player, name, value); // Remove a variável expirada
	                    continue;
	                }
	                
	                player.getVariables().put(name, new PlayerVar(player, name, value, expire_time));
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	
	public static String getVarValue(Player player, String var, String defaultString) {
	    String value = null;
	    String sql = "SELECT value FROM character_memo_alt WHERE obj_id = ? AND name = ?";
	    
	    try (Connection con = ConnectionPool.getConnection();
	         PreparedStatement stmt = con.prepareStatement(sql)) {
	        
	        stmt.setInt(1, player.getObjectId());
	        stmt.setString(2, var);
	        
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                value = rs.getString("value");
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    return value == null ? defaultString : value;
	}

	
	public static String getVarValue(int objectId, String var, String defaultString) {
	    String value = null;
	    String sql = "SELECT value FROM character_memo_alt WHERE obj_id = ? AND name = ?";
	    
	    try (Connection con = ConnectionPool.getConnection();
	         PreparedStatement stmt = con.prepareStatement(sql)) {
	        
	        stmt.setInt(1, objectId);
	        stmt.setString(2, var);
	        
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                value = rs.getString("value");
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    return value == null ? defaultString : value;
	}

}