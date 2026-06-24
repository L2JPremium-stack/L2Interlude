package net.sf.l2j.loginserver;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2j.Config;

public class GameServerListener extends FloodProtectedListener
{
	private static final Logger _log = Logger.getLogger(GameServerListener.class.getName());
	
	private final Set<GameServerThread> _gameServers = ConcurrentHashMap.newKeySet();
	
	public GameServerListener() throws IOException
	{
		super(Config.GAME_SERVER_LOGIN_HOST, Config.GAME_SERVER_LOGIN_PORT);
	}
	
	@Override
	public void addClient(Socket socket)
	{
		if (Config.DEBUG)
			_log.info("Received gameserver connection from: " + socket.getInetAddress().getHostAddress());
		
		final GameServerThread gst = new GameServerThread(socket);
		_gameServers.add(gst);
		
		LoginExecutor.gameServer().execute(gst);
	}
	
	public void removeGameServer(GameServerThread gst)
	{
		if (gst != null)
			_gameServers.remove(gst);
	}
}