package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;

public class AdminAddSpawnZone implements IAdminCommandHandler
{
    private static final String[] ADMIN_COMMANDS = {
        "admin_addspawnzone"
    };

    @Override
    public boolean useAdminCommand(String command, Player activeChar)
    {
    	if (command.equalsIgnoreCase("admin_addspawnzone"))
    	{
    	    L2ZoneType zone = ZoneManager.getInstance().getZone(activeChar);
    	    if (zone == null)
    	    {
    	        activeChar.sendMessage("You are not inside any zone.");
    	        return false;
    	    }

    	    Location spawnLoc = new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ());

    	    try
    	    {
    	        ZoneManager.getInstance().addSpawnToZoneXml(zone, spawnLoc);
    	        activeChar.sendMessage("Spawn added to zone id " + zone.getId());
    	    }
    	    catch (Exception e)
    	    {
    	        activeChar.sendMessage("Error adding spawn: " + e.getMessage());
    	        e.printStackTrace();
    	    }
    	    return true;
    	}
        return false;
    }

    @Override
    public String[] getAdminCommandList()
    {
        return ADMIN_COMMANDS;
    }
}
