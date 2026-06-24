package net.sf.l2j.gameserver.network.serverpackets;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.Player;

/**
 * @author zabbix
 */
public class PartyMemberPosition extends L2GameServerPacket
{
	Map<Integer, Location> locations = new HashMap<>();
	
	public PartyMemberPosition(L2Party party)
	{
		reuse(party);
	}
	
	public void reuse(L2Party party)
	{
		locations.clear();
		for (Player member : party.getPartyMembers())
		{
			if (member == null)
				continue;
			
			locations.put(member.getObjectId(), new Location(member.getX(), member.getY(), member.getZ()));
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xa7);
		writeD(locations.size());
		for (Map.Entry<Integer, Location> entry : locations.entrySet())
		{
			Location loc = entry.getValue();
			writeD(entry.getKey());
			writeD(loc.getX());
			writeD(loc.getY());
			writeD(loc.getZ());
		}
	}
}