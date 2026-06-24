package net.sf.l2j.gameserver.scriptings.scripts.ai.group;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.scriptings.scripts.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.util.Util;

/**
 * AI for mobs in Plains of Dion (near Floran Village)
 */
public final class PlainsOfDion extends L2AttackableAIScript
{
	private static final int MONSTERS[] =
	{
		21104, // Delu Lizardman Supplier
		21105, // Delu Lizardman Special Agent
		21107, // Delu Lizardman Commander
	};
	
	private static final String[] MONSTERS_MSG =
	{
		"$s1! How dare you interrupt our fight! Hey guys, help!",
		"$s1! Hey! We're having a duel here!",
		"The duel is over! Attack!",
		"Foul! Kill the coward!",
		"How dare you interrupt a sacred duel! You must be taught a lesson!"
	};
	
	private static final String[] MONSTERS_ASSIST_MSG =
	{
		"Die, you coward!",
		"Kill the coward!",
		"What are you looking at?"
	};
	
	public PlainsOfDion()
	{
		super("ai/group");
	}
	
	@Override
	protected void registerNpcs()
	{
		addAttackId(MONSTERS);
	}
	
	@Override
	public String onAttack(L2Npc npc, Player player, int damage, boolean isPet, L2Skill skill)
	{
		if (npc.isScriptValue(0))
		{
			npc.broadcastNpcSay(MONSTERS_MSG[Rnd.get(5)].replace("$s1", player.getName()));
			
			for (L2MonsterInstance obj : npc.getKnownList().getKnownTypeInRadius(L2MonsterInstance.class, 300))
			{
				if (Util.contains(MONSTERS, obj.getNpcId()) && !obj.isAttackingNow() && !obj.isDead() && GeoEngine.getInstance().canSeeTarget(npc, obj))
				{
					attack(obj, player);
					obj.broadcastNpcSay(MONSTERS_ASSIST_MSG[Rnd.get(3)]);
				}
			}
			npc.setScriptValue(1);
		}
		return super.onAttack(npc, player, damage, isPet, skill);
	}
}
