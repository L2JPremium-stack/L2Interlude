package net.sf.l2j.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.model.actor.Player;

public final class CharNameTable
{
	private static final Logger LOGGER = Logger.getLogger(CharNameTable.class.getName());
	
	private final Map<Integer, String> idToName = new ConcurrentHashMap<>();
	private final Map<String, Integer> nameToId = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> accessLevels = new ConcurrentHashMap<>();
	
	public static CharNameTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	/* ===================== CACHE API ===================== */
	
	public void register(Player player)
	{
		if (player == null)
			return;
		
		register(player.getObjectId(), player.getName(), player.getAccessLevel().getLevel());
	}
	
	public void unregister(int objectId)
	{
		String name = idToName.remove(objectId);
		if (name != null)
			nameToId.remove(name.toLowerCase());
		
		accessLevels.remove(objectId);
	}
	
	private void register(int objectId, String name, int accessLevel)
	{
		if (name == null || name.isEmpty())
			return;
		
		idToName.put(objectId, name);
		nameToId.put(name.toLowerCase(), objectId);
		accessLevels.put(objectId, accessLevel);
	}
	
	/* ===================== LOOKUPS ===================== */
	
	public String getNameById(int objectId)
	{
		if (objectId <= 0)
			return null;
		
		String name = idToName.get(objectId);
		if (name != null)
			return name;
		
		return loadById(objectId);
	}
	
	public int getIdByName(String name)
	{
		if (name == null || name.isEmpty())
			return -1;
		
		Integer id = nameToId.get(name.toLowerCase());
		if (id != null)
			return id;
		
		return loadByName(name);
	}
	
	public int getAccessLevel(int objectId)
	{
		getNameById(objectId); // garante cache
		return accessLevels.getOrDefault(objectId, 0);
	}
	
	/* ===================== DATABASE ===================== */
	
	private String loadById(int objectId)
	{
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT char_name, accesslevel FROM characters WHERE obj_Id=?"))
		{
			ps.setInt(1, objectId);
			
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					String name = rs.getString(1);
					int access = rs.getInt(2);
					
					register(objectId, name, access);
					return name;
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Failed loading char by id: " + objectId, e);
		}
		return null;
	}
	
	private int loadByName(String name)
	{
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT obj_Id, accesslevel FROM characters WHERE char_name=?"))
		{
			ps.setString(1, name);
			
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					int id = rs.getInt(1);
					int access = rs.getInt(2);
					
					register(id, name, access);
					return id;
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Failed loading char by name: " + name, e);
		}
		return -1;
	}
	
	public static int accountCharNumber(String account)
	{
		int count = 0;
		
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM characters WHERE account_name=?"))
		{
			ps.setString(1, account);
			
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
					count = rs.getInt(1);
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Failed to count characters for account: " + account, e);
		}
		
		return count;
	}
	
	/* ===================== SINGLETON ===================== */
	
	private static class SingletonHolder
	{
		private static final CharNameTable INSTANCE = new CharNameTable();
	}
}
