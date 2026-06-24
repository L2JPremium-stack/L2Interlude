package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Playable;

public class AttackableKnownList extends NpcKnownList
{
	public AttackableKnownList(Attackable activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public boolean removeKnownObject(L2Object object)
	{
		if (!super.removeKnownObject(object))
			return false;
		
		// get attackable
		final Attackable attackable = (Attackable) _activeObject;
		
		// remove object from agro list
		if (object instanceof Creature)
			attackable.getAggroList().remove(object);
		
		// check AI for players and set AI to idle
		if (attackable.hasAI() && getKnownType(Player.class).isEmpty())
			attackable.getAI().setIntention(CtrlIntention.IDLE, null);
		
		return true;
	}
	
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		if (!(object instanceof Creature))
			return 0;
		
		if (object instanceof Playable)
			return object.getKnownList().getDistanceToWatchObject(_activeObject);
		
		// get attackable
		final Attackable attackable = (Attackable) _activeObject;
		
		return Math.max(300, Math.max(attackable.getTemplate().getAggroRange(), attackable.getTemplate().getClanRange()));
	}
	
	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		return (int) (getDistanceToWatchObject(object) * 1.5);
	}
}