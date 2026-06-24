package net.sf.l2j.timezone;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.gameserver.model.zone.type.L2TimeFarmZone;

/**
 * @author Sarada
 *
 */
public class AdminSpawnTimeZone implements IAdminCommandHandler
{
	private static String[] _adminCommands = { "admin_loc_timezone" };

	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equalsIgnoreCase("admin_loc_timezone"))
		{

		    for (L2ZoneType zone : ZoneManager.getInstance().getZones(activeChar))
		    {
		        if (zone instanceof L2TimeFarmZone)
		        {
		            L2TimeFarmZone tfz = (L2TimeFarmZone) zone;
		            String zoneName = tfz.getZoneName();
		            TimeFarmZoneData.ZoneInfo zoneInfo = TimeFarmZoneData.getZoneInfo(zoneName);

		            if (zoneInfo != null)
		            {
		                Location loc = new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ());

		                // mobId 0 = mobId=""
		                zoneInfo.spawns.add(new TimeFarmZoneData.MobSpawn(0, loc));

		                TimeFarmZoneData.save();

		                activeChar.sendMessage("Localização salva para zona: " + zoneName);
		                activeChar.sendMessage("Coordenadas: " + loc.getX() + " " + loc.getY() + " " + loc.getZ());
		            }
		            else
		            {
		                activeChar.sendMessage("Zona '" + zoneName + "' nao encontrada em TimeFarmZoneData.");
		            }

		            return true;
		        }
		    }

		    activeChar.sendMessage("Você nao esta dentro de uma TimeFarmZone.");
		    return true;
		}

		return false;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
	
}
