package net.sf.l2j.gameserver.instancemanager.custom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;

public class ChatBanManager
{
	private static final Logger _log = Logger.getLogger(ChatBanManager.class.getName());
	
	private final Map<Integer, Long> _blocklist;
	protected final Map<Integer, Long> _blocksTask;
	private ScheduledFuture<?> _scheduler;
	
	public static ChatBanManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected ChatBanManager()
	{
		_blocklist = new ConcurrentHashMap<>();
		_blocksTask = new ConcurrentHashMap<>();
		startScheduler();
		load();
	}
	
	public void reload()
	{
		_blocklist.clear();
		_blocksTask.clear();
		if (_scheduler != null)
			_scheduler.cancel(true);
		startScheduler();
		load();
	}
	
	private synchronized void startScheduler()
	{
		if (_scheduler != null)
		{
			_scheduler.cancel(false);
			_scheduler = null;
		}
		_scheduler = ThreadPool.scheduleAtFixedRate(new BlockTask(), 1000, 1000);
	}
	
	public void load()
	{
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT objectId, duration FROM ban_hwid_chat ORDER BY hwid");
			ResultSet rs = statement.executeQuery();
			while (rs.next())
				_blocklist.put(rs.getInt("objectId"), rs.getLong("duration"));
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Exception: ChatBanManager load: " + e.getMessage());
		}
		
		_log.info("ChatBanManager: Loaded " + _blocklist.size() + " banned hwids.");
	}
	
	public void addBlock(int objectId, long duration, String hwid, String login, String name)
	{
		_blocklist.put(objectId, duration);
		_blocksTask.put(objectId, duration);
		addBlockPlayer(objectId, true);
		
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("INSERT INTO ban_hwid_chat (objectId, duration, hwid, account_name, char_name) VALUES (?, ?, ?,?,?)");
			statement.setInt(1, objectId);
			statement.setLong(2, duration);
			statement.setString(3, hwid);
			statement.setString(4, login);
			statement.setString(5, name);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Exception: ChatBanManager addBlock: " + e.getMessage());
		}
	}
	
	public void updateBlock(int objectId, long duration)
	{
		duration += _blocklist.getOrDefault(objectId, 0L);
		_blocklist.put(objectId, duration);
		_blocksTask.put(objectId, duration);
		
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE ban_hwid_chat SET duration = ? WHERE hwid = ?");
			statement.setLong(1, duration);
			statement.setInt(2, objectId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Exception: ChatBanManager updateBlock: " + e.getMessage());
		}
	}
	
	public void removeBlock(int objectId)
	{
		_blocklist.remove(objectId);
		_blocksTask.remove(objectId);
		
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM ban_hwid_chat WHERE objectId = ?");
			statement.setInt(1, objectId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Exception: ChatBanManager removeBlock: " + e.getMessage());
		}
	}
	
	public boolean hasBlockPrivileges(int objectId)
	{
		return _blocklist.containsKey(objectId);
	}
	
	public long getBlockDuration(int objectId)
	{
		return _blocklist.get(objectId);
	}
	
	public void addBlockTask(int objectId, long duration)
	{
		_blocksTask.put(objectId, duration);
	}
	
	public void removeBlockTask(int objectId)
	{
		_blocksTask.remove(objectId);
	}
	
	public void addBlockPlayer(int objectId, boolean apply)
	{
		final Player player = L2World.getInstance().getPlayer(objectId);
		player.setChatBlock(true);
	}
	
	public class BlockTask implements Runnable
	{
		@Override
		public final void run()
		{
			if (_blocksTask.isEmpty())
				return;
			
			for (Map.Entry<Integer, Long> entry : _blocksTask.entrySet())
			{
				final long duration = entry.getValue();
				if (System.currentTimeMillis() > duration)
				{
					final int objectId = entry.getKey();
					removeBlock(objectId);
					
					final Player player = L2World.getInstance().getPlayer(objectId);
					player.setChatBlock(false);
				}
			}
		}
	}
	
	private static class SingletonHolder
	{
		protected static final ChatBanManager _instance = new ChatBanManager();
	}
}