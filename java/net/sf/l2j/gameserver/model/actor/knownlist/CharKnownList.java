package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.Creature;

public class CharKnownList extends ObjectKnownList
{
	public CharKnownList(Creature activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public boolean removeKnownObject(L2Object object)
	{
		if (!super.removeKnownObject(object))
			return false;
		
		// get character
		final Creature character = (Creature) _activeObject;
		
		// If object is targeted by the L2Character, cancel Attack or Cast
		if (object == character.getTarget())
			character.setTarget(null);
		
		return true;
	}
	
	/**
	 * Remove all objects from known list, cancel target and inform AI.
	 */
	@Override
	public final void removeAllKnownObjects()
	{
		super.removeAllKnownObjects();
		
		// get character
		final Creature character = (Creature) _activeObject;
		
		// set target to null
		character.setTarget(null);
		
		// cancel AI task
		if (character.hasAI())
			character.setAI(null);
	}
}