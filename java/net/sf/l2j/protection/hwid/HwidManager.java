package net.sf.l2j.protection.hwid;

import java.util.UUID;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.protection.hwid.HwidDAO.BanSnapshot;

public class HwidManager
{
	protected static final Logger LOGGER = Logger.getLogger(HwidManager.class.getName());
	
	private static final HwidManager INSTANCE = new HwidManager();

	private final HwidDAO dao = new HwidDAO();
	
	private HwidManager()
	{
		ThreadPool.scheduleAtFixedRate("HwidBanMonitor", this::enforceActiveBans, 3000L, 3000L);
	}
	
	public static HwidManager getInstance()
	{
		return INSTANCE;
	}
	
	private static final String SECRET = "BAN_L2JDEV_2070";
	
	public boolean validateClient(L2GameClient client, String hdd, String mac, String cpu, String key)
	{
	    if (client == null)
	        return false;

	    if (hdd == null || mac == null || cpu == null || key == null)
	        return false;

	    hdd = hdd.trim().toLowerCase();
	    mac = mac.trim().toLowerCase();
	    cpu = cpu.trim().toLowerCase();
	    key = key.trim();

	    if (hdd.isEmpty() || mac.isEmpty() || cpu.isEmpty() || key.isEmpty())
	        return false;

	    // VALIDA KEY PRIMEIRO
	    if (!key.equals(SECRET))
	    {
	        if (Config.PACKET_HANDLER_DEBUG)
	            LOGGER.warning("HWID key invalida de " + client + ".");
	        return false;
	    }

	    if (dao.isHardwareBanned(cpu, hdd, mac))
	    {
	        LOGGER.warning("HARDWARE BANIDO: " + hdd + "/" + mac);
	        return false;
	    }

	    final int deviceId = dao.getOrCreateDevice(cpu, hdd, mac);
	    if (deviceId == -1)
	        return false;

	    if (dao.isBanned(deviceId))
	    {
	        LOGGER.warning("HWID BANIDO: " + deviceId);
	        return false;
	    }

	    dao.updateLastSeen(deviceId);

	    client.setHwidSession(new HwidSession(deviceId, cpu, hdd, mac));

	  
	    return true;
	}
	
	public boolean isAccessBlocked(L2GameClient client, String account)
	{
		if (client == null || client.getHwidSession() == null)
			return true;
		
		final HwidSession session = client.getHwidSession();
		return dao.isBanned(session.getDeviceId()) || dao.isHardwareBanned(session.getCpu(), session.getHdd(), session.getMac()) || dao.isAccountBanned(account);
	}
	
	public void onEnterWorld(L2GameClient client)
	{
		if (client.getHwidSession() == null)
			return;
		
		String account = client.getAccountName();
		int deviceId = client.getHwidSession().getDeviceId();
		String ip = client.getConnection().getInetAddress().getHostAddress();
		
		String token = UUID.randomUUID().toString();
		
		dao.linkAccount(account, deviceId);
		
		dao.createSession(account, deviceId, ip, token);
	}
	
	public boolean isMacAlreadyOnline(L2GameClient client)
	{
	    if (client == null || client.getHwidSession() == null)
	        return false;

	    String mac = client.getHwidSession().getMac();

	    for (Player player : L2World.getInstance().getPlayers())
	    {
	        if (player == null || player.getClient() == null)
	            continue;

	        if (player.getClient().getHwidSession() == null)
	            continue;

	        String otherMac = player.getClient().getHwidSession().getMac();

	        // IGNORA O PRÓPRIO PLAYER
	        if (player.getClient() == client)
	            continue;

	        if (mac.equalsIgnoreCase(otherMac))
	        {
	            return true;
	        }
	    }

	    return false;
	}
	
	private void enforceActiveBans()
	{
		final BanSnapshot bans = dao.loadBanSnapshot();
		
		for (Player player : L2World.getInstance().getPlayers())
		{
			if (player == null)
				continue;
			
			final L2GameClient client = player.getClient();
			if (client == null || client.getHwidSession() == null)
				continue;
			
			final int deviceId = client.getHwidSession().getDeviceId();
			final String account = player.getAccountName();
			if (!bans.containsDevice(deviceId) && !bans.containsAccount(account))
				continue;
			
			LOGGER.warning("Closing permanently banned client: account=" + account + ", device=" + deviceId);
			dao.deactivateSessions(account, deviceId);
			client.closeNow();
		}
	}
}
