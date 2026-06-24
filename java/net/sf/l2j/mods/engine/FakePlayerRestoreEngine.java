package net.sf.l2j.mods.engine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.datatables.CharNameTable;
import net.sf.l2j.mods.actor.FakePlayer;
import net.sf.l2j.mods.manager.FakePlayerManager;

 
public class FakePlayerRestoreEngine
{
	private static final Logger LOGGER = Logger.getLogger(FakePlayerRestoreEngine.class.getName());
	private static final String SELECT_FAKE_CHARACTERS = "SELECT c.obj_id FROM characters c INNER JOIN accounts a ON a.login = c.account_name WHERE a.access_level = -1 AND a.login LIKE 'AutoPilot_%'";
	
	public void collectAll()
	{
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement(SELECT_FAKE_CHARACTERS); ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				int objectId = rs.getInt("obj_id");
				if (FakePlayerManager.getInstance().getPlayer(objectId) == null)
					FakePlayerRestoreQueue.add(objectId);
			}
		}
		catch (Exception e)
		{
			LOGGER.severe("Error collecting fake players: " + e.getMessage());
		}
	}
	
	public FakePlayer restoreSingle(int objectId)
	{
		FakePlayer fake = null;
		try
		{
			FakePlayer current = FakePlayerManager.getInstance().getPlayer(objectId);
			if (current != null)
				return current;
			fake = FakePlayer.restore(objectId);
			if (fake == null)
				throw new IllegalStateException("Character data not found.");
			return activate(fake);
		}
		catch (Exception e)
		{
			if (fake != null && !fake.isDestroyed())
				fake.deleteMe();
			LOGGER.log(Level.WARNING, "Failed restoring fake " + objectId + ": " + e.getMessage(), e);
			return null;
		}
	}
	public synchronized FakePlayer activate(FakePlayer fake)
	{
		if (fake == null)
			throw new IllegalArgumentException("FakePlayer cannot be null.");
		FakePlayer current = FakePlayerManager.getInstance().getPlayer(fake.getObjectId());
		if (current != null)
			return current;
		if (!FakePlayerManager.getInstance().registerIfAbsent(fake))
			return FakePlayerManager.getInstance().getPlayer(fake.getObjectId());
		try
		{
			CharNameTable.getInstance().register(fake);
			fake.spawnMe();
			fake.assignDefaultAI();
			fake.setOnlineStatus(true, true);
			return fake;
		}
		catch (Exception e)
		{
			fake.deleteMe();
			throw new IllegalStateException("Could not activate FakePlayer " + fake.getObjectId() + ".", e);
		}
	}
	
	public static FakePlayerRestoreEngine getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		private static final FakePlayerRestoreEngine INSTANCE = new FakePlayerRestoreEngine();
	}
	
}
