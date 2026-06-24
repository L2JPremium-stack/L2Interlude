package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ai.model.L2CharacterAI;
import net.sf.l2j.gameserver.ai.model.L2NpcWalkerAI;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;

/**
 * This class manages npcs who can walk using nodes.
 * @author Rayan RPG, JIV
 */
public class L2NpcWalkerInstance extends L2NpcInstance
{
	public L2NpcWalkerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		
		setAI(new L2NpcWalkerAI(this));
	}
	
	@Override
	public void setAI(L2CharacterAI newAI)
	{
		// AI can't be detached, npc must move with the same AI instance.
		if (!(_ai instanceof L2NpcWalkerAI))
			_ai = newAI;
	}
	
	@Override
	public void reduceCurrentHp(double i, Creature attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		return false;
	}
	
	@Override
	public L2NpcWalkerAI getAI()
	{
		return (L2NpcWalkerAI) _ai;
	}
	
	@Override
	public void detachAI()
	{
		// AI can't be detached.
	}
}