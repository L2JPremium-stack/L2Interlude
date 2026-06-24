package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.instancemanager.games.MonsterRace;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.L2RaceManagerInstance;
import net.sf.l2j.gameserver.network.serverpackets.DeleteObject;

public class RaceManagerKnownList extends NpcKnownList
{
	public RaceManagerKnownList(L2RaceManagerInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public boolean addKnownObject(L2Object object)
	{
		if (!super.addKnownObject(object))
			return false;
		
		if (object instanceof Player)
			((Player) object).sendPacket(MonsterRace.getInstance().getRacePacket());
		
		return true;
	}
	
	@Override
	public boolean removeKnownObject(L2Object object)
	{
		if (!super.removeKnownObject(object))
			return false;
		
		if (object instanceof Player)
		{
			// get player
			final Player player = ((Player) object);
			
			// for all monster race NPCs
			for (L2Npc npc : MonsterRace.getInstance().getMonsters())
				player.sendPacket(new DeleteObject(npc));
		}
		
		return true;
	}
}