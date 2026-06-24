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

public class ChatHeroManager
{
	private static final Logger _log = Logger.getLogger(ChatHeroManager.class.getName());
	
	private final Map<Integer, Long> _chats;
	protected final Map<Integer, Long> _chatsTask;
	private ScheduledFuture<?> _scheduler;
	
	public static ChatHeroManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected ChatHeroManager()
	{
		_chats = new ConcurrentHashMap<>();
		_chatsTask = new ConcurrentHashMap<>();
		startScheduler();
		load();
	}
	
	public void reload()
	{
		_chats.clear();
		_chatsTask.clear();
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
		_scheduler = ThreadPool.scheduleAtFixedRate(new ChatTask(), 1000, 1000);
	}
	
	public void load()
	{
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT objectId, duration FROM chat_hero_manager ORDER BY objectId");
			ResultSet rs = statement.executeQuery();
			while (rs.next())
				_chats.put(rs.getInt("objectId"), rs.getLong("duration"));
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Exception: ChatManager load: " + e.getMessage());
		}
		
		_log.info("ChatHero_Manager: Loaded " + _chats.size() + " characters with chat time.");
	}
	
	public void addChatTime(int objectId, long duration)
	{
		_chats.put(objectId, duration);
		_chatsTask.put(objectId, duration);
		
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("INSERT INTO chat_hero_manager (objectId, duration) VALUES (?, ?)");
			statement.setInt(1, objectId);
			statement.setLong(2, duration);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Exception: addChatTime: " + e.getMessage());
		}
	}
	
	public void updateChatTime(int objectId, long duration)
	{
		duration += _chats.getOrDefault(objectId, 0L);
		_chats.put(objectId, duration);
		_chatsTask.put(objectId, duration);
		
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE chat_hero_manager SET duration = ? WHERE objectId = ?");
			statement.setLong(1, duration);
			statement.setInt(2, objectId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("updateChatTime: " + e.getMessage());
		}
	}
	
	public void removeChatTime(int objectId)
	{
		_chats.remove(objectId);
		_chatsTask.remove(objectId);
		
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM chat_hero_manager WHERE objectId = ?");
			statement.setInt(1, objectId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("removeChatTime: " + e.getMessage());
		}
	}
	
	public boolean hasChatPrivileges(int objectId)
	{
		return _chats.containsKey(objectId);
	}
	
	public long getChatDuration(int objectId)
	{
		return _chats.get(objectId);
	}
	
	public void addChatTask(int objectId, long duration)
	{
		_chatsTask.put(objectId, duration);
	}
	
	public void removeChatTask(int objectId)
	{
		_chatsTask.remove(objectId);
	}
	
	public class ChatTask implements Runnable
	{
		@Override
		public final void run()
		{
			if (_chatsTask.isEmpty())
				return;
			
			for (Map.Entry<Integer, Long> entry : _chatsTask.entrySet())
			{
				final long duration = entry.getValue();
				if (System.currentTimeMillis() > duration)
				{
					final int objectId = entry.getKey();
                    removeChatTime(objectId);
					final Player player = L2World.getInstance().getPlayer(objectId);	
					player.sendMessage("Hero chat available.");
				}
			}
		}
	}
	
	private static class SingletonHolder
	{
		protected static final ChatHeroManager _instance = new ChatHeroManager();
	}
}