package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.L2FriendlyMobInstance;

public class FriendlyMobKnownList extends AttackableKnownList
{
	public FriendlyMobKnownList(L2FriendlyMobInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public boolean addKnownObject(L2Object object)
	{
		if (!super.addKnownObject(object))
			return false;
		
		// object is player
		if (object instanceof Player)
		{
			// get friendly monster
			final L2FriendlyMobInstance monster = (L2FriendlyMobInstance) _activeObject;
			
			// AI is idle, set AI
			if (monster.getAI().getIntention() == CtrlIntention.IDLE)
				monster.getAI().setIntention(CtrlIntention.ACTIVE, null);
		}
		
		return true;
	}
	
	@Override
	public boolean removeKnownObject(L2Object object)
	{
		if (!super.removeKnownObject(object))
			return false;
		
		if (!(object instanceof Creature))
			return true;
		
		// get friendly monster
		final L2FriendlyMobInstance monster = (L2FriendlyMobInstance) _activeObject;
		
		if (monster.hasAI())
		{
			monster.getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
			if (monster.getTarget() == (Creature) object)
				monster.setTarget(null);
		}
		
		if (monster.isVisible() && getKnownType(Player.class).isEmpty())
		{
			monster.getAggroList().clear();
			if (monster.hasAI())
				monster.getAI().setIntention(CtrlIntention.IDLE, null);
		}
		
		return true;
	}
}