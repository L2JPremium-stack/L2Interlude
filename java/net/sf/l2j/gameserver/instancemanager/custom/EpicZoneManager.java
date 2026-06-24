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
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.model.zone.type.L2BossZone;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

public class EpicZoneManager
{
    private static final Logger _log = Logger.getLogger(EpicZoneManager.class.getName());

	public EpicZoneManager()
    {
        _log.log(Level.INFO, "EpicZoneManager - Loaded.");
    }
	
	private static boolean checkPlayersKickTask(Player activeChar, Integer numberBox, L2BossZone zone)
	{
	    Map<String, List<Player>> map = new HashMap<>();

	    if (activeChar != null && zone != null)
	    {
	        String hwid = activeChar.getHWID();

	        for (Player player : L2World.getInstance().getPlayers())
	        {
	            // Somente players dentro da zona
	            if (!zone.isCharacterInZone(player))
	                continue;

	            if (!player.isInsideZone(ZoneId.BOSS) || player.getHWID() == null)
	                continue;

	            if (hwid.equals(player.getHWID()))
	            {
	                map.computeIfAbsent(hwid, k -> new ArrayList<>()).add(player);

	                if (map.get(hwid).size() > numberBox)
	                    return true;
	            }
	        }
	    }
	    return false;
	}

    
	public boolean checkPlayersArea(Player activeChar, L2BossZone zone, Integer numberBox, Boolean forcedTeleport)
	{
	    if (checkPlayersKickTask(activeChar, numberBox, zone))
	    {
	        if (forcedTeleport)
	        {
	            activeChar.sendPacket(new CreatureSay(0, Say2.TELL, "SYS","Allowed only " + numberBox + " Client in Epic Zone!"));
	            for (Player allgms : GmListTable.getInstance().getAllGms(true))
	            {
	                if (!activeChar.isGM())
	                    allgms.sendPacket(new CreatureSay(0, Say2.TELL, "[Double HWID]", activeChar.getName() + " in Epic Zone!"));
	            }
	            RandomTeleport(activeChar);
	        }
	        return true;
	    }
	    return false;
	}
    
//    private static boolean checkPlayersKickTask_ip(L2PcInstance activeChar, Integer numberBox, Collection<L2PcInstance> world)
//    {
//        Map<String, List<L2PcInstance>> ipMap = new HashMap<>();
//        
//        for (L2PcInstance player : world)
//        {
//            if (!player.isInsideZone(ZoneId.BOSS) || player.getHWID() == null || player.getClient().isDetached())
//                continue;
//            
//            String playerIp = player.getHWID();
//            
//            if (IPProtection(playerIp))
//            {
//                if (ipMap.get(playerIp) == null)
//                    ipMap.put(playerIp, new ArrayList<>()); // diamond operator aqui
//                
//                ipMap.get(playerIp).add(player);
//                
//                if (ipMap.get(playerIp).size() >= numberBox)
//                    return true;
//            }
//        }
//        return false;
//    }
	private static boolean checkPlayersKickTask_ip(Player activeChar, Integer numberBox, Collection<Player> world)
	{
	    Map<String, List<Player>> ipMap = new HashMap<>();
	    
	    for (Player player : world)
	    {
	        if (!player.isInsideZone(ZoneId.RAID_ZONE) || player.getHWID() == null || player.getClient().isDetached())
	            continue;
	        
	        String playerIp = player.getHWID();
	        
	        if (IPProtection(playerIp))
	        {
	            ipMap.computeIfAbsent(playerIp, k -> new ArrayList<>()).add(player);
	            
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
            	activeChar.sendPacket(new CreatureSay(0, Say2.TELL, "SYS"," Double box not allowed in Epic Zone!"));
				for (Player allgms : GmListTable.getInstance().getAllGms(true))
				{
					if (!activeChar.isGM())
						allgms.sendPacket(new CreatureSay(0, Say2.TELL, "[Double IP]", activeChar.getName() +" Epic Raid Zone!"));
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
			final PreparedStatement statement = con.prepareStatement("SELECT ip FROM ip_protect_epiczone WHERE ip=?");
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
        protected static final EpicZoneManager _instance = new EpicZoneManager();
    }

    public static final EpicZoneManager getInstance()
    {
        return SingletonHolder._instance;
    }

    public boolean checkClanArea(Player activeChar, L2BossZone zone, Integer numberBox, Boolean forcedTeleport)
    {
        if (checkClanAreaKickTask(activeChar, numberBox, zone))
        {
            if (forcedTeleport)
            {
                activeChar.sendPacket(new ExShowScreenMessage("Allowed only " + numberBox + " clans members on this area!", 6 * 1000));
                RandomTeleport(activeChar);
            }
            return true;
        }
        return false;
    }

    private static boolean checkClanAreaKickTask(Player activeChar, Integer numberBox, L2BossZone zone)
    {
        Map<String, List<Player>> zergMap = new HashMap<>();

        L2Clan clan = activeChar.getClan();

        if (clan != null)
        {
            String clanName = clan.getName();

            for (Player player : clan.getOnlineMembers())
            {
                if (!zone.isCharacterInZone(player) || player.getClan() == null)
                    continue;

                if (clanName.equals(player.getClan().getName()))
                {
                    zergMap.computeIfAbsent(clanName, k -> new ArrayList<>()).add(player);

                    if (zergMap.get(clanName).size() > numberBox)
                        return true;
                }
            }
        }
        return false;
    }


    public boolean checkAllyArea(Player activeChar, L2BossZone zone, Integer numberBox, Collection<Player> world, Boolean forcedTeleport)
    {
        if (checkAllyAreaKickTask(activeChar, numberBox, world, zone))
        {
            if (forcedTeleport)
            {
                activeChar.sendPacket(new ExShowScreenMessage("Allowed only " + numberBox + " ally members on this area!", 6 * 1000));
                RandomTeleport(activeChar);
            }
            return true;
        }
        return false;
    }

    private static boolean checkAllyAreaKickTask(Player activeChar, Integer numberBox, Collection<Player> world, L2BossZone zone)
    {
        Map<String, List<Player>> zergMap = new HashMap<>();

        if (activeChar.getAllyId() != 0)
        {
            String activeAllyName = activeChar.getClan().getAllyName();

            for (Player player : world)
            {
                // Aqui o filtro para estar na zona específica
                if (!zone.isCharacterInZone(player) || player.getAllyId() == 0)
                    continue;

                String playerAllyName = player.getClan().getAllyName();

                if (activeAllyName.equals(playerAllyName))
                {
                    zergMap.computeIfAbsent(activeAllyName, k -> new ArrayList<>()).add(player);

                    if (zergMap.get(activeAllyName).size() > numberBox)
                        return true;
                }
            }
        }
        return false;
    }

}