package net.sf.l2j.loginserver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class LoginExecutor
{
	private static final ExecutorService GAME_SERVER_EXECUTOR = Executors.newCachedThreadPool(r ->
	{
		final Thread t = new Thread(r, "LoginServer-GameServerConnection");
		t.setDaemon(false);
		return t;
	});
	
	private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(2, r ->
	{
		final Thread t = new Thread(r, "LoginServer-Scheduler");
		t.setDaemon(true);
		return t;
	});
	
	public static ExecutorService gameServer()
	{
		return GAME_SERVER_EXECUTOR;
	}
	
	public static ScheduledExecutorService scheduler()
	{
		return SCHEDULER;
	}
	
	public static void shutdown()
	{
		GAME_SERVER_EXECUTOR.shutdownNow();
		SCHEDULER.shutdownNow();
	}
	
	private LoginExecutor()
	{
	}
}