package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.model.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;

public class MonsterKnownList extends AttackableKnownList
{
	public MonsterKnownList(L2MonsterInstance activeChar)
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
			// get monster AI
			final L2CharacterAI ai = ((L2MonsterInstance) _activeObject).getAI();
			
			// AI exists and is idle, set active
			if (ai != null && ai.getIntention() == CtrlIntention.IDLE)
				ai.setIntention(CtrlIntention.ACTIVE, null);
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
		
		// get monster
		final L2MonsterInstance monster = (L2MonsterInstance) _activeObject;
		
		// monster has AI, inform about lost object
		if (monster.hasAI())
			monster.getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
		
		// clear agro list
		if (monster.isVisible() && getKnownType(Player.class).isEmpty())
			monster.getAggroList().clear();
		
		return true;
	}
}