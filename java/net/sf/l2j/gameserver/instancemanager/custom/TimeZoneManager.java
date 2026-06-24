package net.sf.l2j.gameserver.instancemanager.custom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.datatables.GmListTable;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.model.zone.type.L2TimeFarmZone;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class TimeZoneManager
{
    private static final Logger _log = Logger.getLogger(TimeZoneManager.class.getName());

	public TimeZoneManager()
    {
        _log.log(Level.INFO, "TimeZoneProtectionManager - Loaded.");
    }
	
	@SuppressWarnings("unused")
	private static boolean checkPlayersKickTask(Player activeChar, Integer numberBox)
	{
	    Map<String, List<Player>> map = new HashMap<>();
	    
	    if (activeChar != null)
	    {
	        for (Player player : L2World.getInstance().getPlayers())
	        {
	            if (!player.isInsideZone(ZoneId.TIME_FARM_ZONE) || player.getHWID() == null)
	                continue;

	            String ip1 = activeChar.getHWID();
	            String ip2 = player.getHWID();

	            if (ip1.equals(ip2))
	            {
	                if (map.get(ip1) == null)
	                    map.put(ip1, new ArrayList<>());

	                map.get(ip1).add(player);

	                if (map.get(ip1).size() > numberBox)
	                    return true;
	            }
	        }
	    }
	    return false;
	}

    
	public boolean checkPlayersArea(Player activeChar, Integer numberBox, L2TimeFarmZone zone)
	{
	    int count = 0;
	    String hwid = activeChar.getHWID(); // seu método para pegar o HWID do player

	    for (Player plr : L2World.getInstance().getPlayers())
	    {
	        if (plr == null || plr == activeChar || plr.getHWID() == null)
	            continue;

	        if (!hwid.equals(plr.getHWID()))
	            continue;

	        // Apenas conta se estiver dentro da mesma zona
	        if (!zone.isInsideZone(plr))
	            continue;

	        count++;
	    }

	    if (count >= numberBox)
	    {
	        activeChar.sendPacket(new CreatureSay(0, Say2.TELL, "SYS", "Allowed only " + numberBox + " Client in Time Farm Zone!"));

	        for (Player allgms : GmListTable.getInstance().getAllGms(true))
	        {
	            if (!activeChar.isGM())
	            {
	                allgms.sendPacket(new CreatureSay(0, Say2.TELL, "[Double HWID]", activeChar.getName() + " in Time Farm Zone!"));
	            }
	        }

	        RandomTeleport(activeChar);
	        return true;
	    }

	    return false;
	}

    private static boolean checkPlayersKickTask_ip(Player activeChar, Integer numberBox, Collection<Player> world)
    {
        Map<String, List<Player>> ipMap = new HashMap<>();
        
        for (Player player : world)
        {
            if (!player.isInsideZone(ZoneId.TIME_FARM_ZONE) || player.getHWID() == null || player.getClient().isDetached())
                continue;
            
            String playerIp = player.getHWID();
            
            if (IPProtection(playerIp))
            {
                if (ipMap.get(playerIp) == null)
                    ipMap.put(playerIp, new ArrayList<>()); // diamond operator aqui
                
                ipMap.get(playerIp).add(player);
                
                if (ipMap.get(playerIp).size() >= numberBox)
                    return true;
            }
        }
        return false;
    }


    public boolean checkPlayersArea_ip(Player activeChar, Integer numberBox, Collection<Player> world, Boolean forcedLogOut)
    {
        if (checkPlayersKickTask_ip(activeChar, numberBox, world))
        {
            if (forcedLogOut)
            {
            	activeChar.sendPacket(new CreatureSay(0, Say2.TELL, "SYS"," Double box not allowed in Time Farm Zone!"));
				for (Player allgms : GmListTable.getInstance().getAllGms(true))
				{
					if (!activeChar.isGM())
						allgms.sendPacket(new CreatureSay(0, Say2.TELL, "[Double IP]", activeChar.getName() +" Time Farm Zone!"));
				}
            	RandomTeleport(activeChar);
            }
            return true;
        }
        return false;
    }

	public static synchronized boolean IPProtection(final String ip)
	{
		boolean result = true;
		try (final Connection con = ConnectionPool.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT ip FROM ip_protect_timezone WHERE ip=?");
			statement.setString(1, ip);
			final ResultSet rset = statement.executeQuery();
			result = rset.next();
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			System.out.println("SelectorHelper -> IPProtection: " + e.getMessage());
		}
		return result;
	}
    
    //Giran Coord's
	public void RandomTeleport(Player activeChar)
	{
		switch (Rnd.get(5))
		{
		    case 0:
		    {
		    	int x = 82533 + Rnd.get(100);
		    	int y = 149122 + Rnd.get(100);
		    	activeChar.teleToLocation(x, y, -3474, 0);
		    	break;
		    }
		    case 1:
		    {
		    	int x = 82571 + Rnd.get(100);
		    	int y = 148060 + Rnd.get(100);
		    	activeChar.teleToLocation(x, y, -3467, 0);
		    	break;
		    }
		    case 2:
		    {
		    	int x = 81376 + Rnd.get(100);
		    	int y = 148042 + Rnd.get(100);
		    	activeChar.teleToLocation(x, y, -3474, 0);
		    	break;
		    }
		    case 3:
		    {
		    	int x = 81359 + Rnd.get(100);
		    	int y = 149218 + Rnd.get(100);
		    	activeChar.teleToLocation(x, y, -3474, 0);
		    	break;
		    }
		    case 4:
		    {
		    	int x = 82862 + Rnd.get(100);
		    	int y = 148606 + Rnd.get(100);
		    	activeChar.teleToLocation(x, y, -3474, 0);
		    	break;
		    }
	    }
	}
	
    private static class SingletonHolder
    {
        protected static final TimeZoneManager _instance = new TimeZoneManager();
    }

    public static final TimeZoneManager getInstance()
    {
        return SingletonHolder._instance;
    }
}