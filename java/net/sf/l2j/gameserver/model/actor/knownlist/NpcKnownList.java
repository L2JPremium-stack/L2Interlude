package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.L2FestivalGuideInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;

public class NpcKnownList extends CharKnownList
{
	public NpcKnownList(L2Npc activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		// object is not L2Character or object is L2NpcInstance, skip
		if (object instanceof L2NpcInstance || !(object instanceof Creature))
			return 0;
		
		if (object instanceof Playable)
		{
			// known list owner if L2FestivalGuide, use extended range
			if (_activeObject instanceof L2FestivalGuideInstance)
				return 4000;
			
			// default range to keep players
			return 1500;
		}
		
		return 500;
	}
	
	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		return 2 * getDistanceToWatchObject(object);
	}
}