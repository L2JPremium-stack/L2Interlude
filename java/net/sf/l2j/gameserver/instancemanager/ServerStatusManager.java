package net.sf.l2j.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.sf.l2j.gameserver.ConnectionPool;

public final class ServerStatusManager
{
	public enum ServerType
	{
		LOGIN,
		GAME
	}
	
	public enum ServerStatus
	{
		ONLINE,
		OFFLINE
	}
	
	private static final long HEARTBEAT_INTERVAL = 30; // segundos
	
	private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread t = new Thread(r, "ServerStatusHeartbeat");
		t.setDaemon(true);
		return t;
	});
	
	private final ServerType _type;
	private ScheduledFuture<?> _heartbeatTask;
	
	private ServerStatusManager(ServerType type)
	{
		_type = type;
	}
	
	/*
	 * ========================= START =========================
	 */
	public static ServerStatusManager start(ServerType type)
	{
		ServerStatusManager mgr = new ServerStatusManager(type);
		mgr.setOnline();
		mgr.startHeartbeat();
		return mgr;
	}
	
	/*
	 * ========================= ONLINE =========================
	 */
	private void setOnline()
	{
		execute("UPDATE server_status " + "SET status='ONLINE', last_start=NOW(), last_heartbeat=NOW() " + "WHERE server_type=?");
	}
	
	/*
	 * ========================= OFFLINE =========================
	 */
	public void shutdown()
	{
		if (_heartbeatTask != null)
			_heartbeatTask.cancel(false);
		
		execute("UPDATE server_status " + "SET status='OFFLINE', last_stop=NOW() " + "WHERE server_type=?");
	}
	
	/*
	 * ========================= HEARTBEAT =========================
	 */
	private void startHeartbeat()
	{
		_heartbeatTask = SCHEDULER.scheduleAtFixedRate(() -> execute("UPDATE server_status SET last_heartbeat=NOW() WHERE server_type=?"), HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
	}
	
	/*
	 * ========================= SQL EXEC =========================
	 */
	private void execute(String sql)
	{
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement(sql))
		{
			ps.setString(1, _type.name());
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public ServerStatus getStatus()
	{
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT status FROM server_status WHERE server_type=?"))
		{
			ps.setString(1, _type.name());
			
			try (var rs = ps.executeQuery())
			{
				if (rs.next())
					return ServerStatus.valueOf(rs.getString("status"));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return ServerStatus.OFFLINE; // fallback seguro
	}
	
	public boolean isOnline()
	{
		return getStatus() == ServerStatus.ONLINE;
	}
}
