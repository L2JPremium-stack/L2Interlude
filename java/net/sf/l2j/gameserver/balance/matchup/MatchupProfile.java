package net.sf.l2j.gameserver.balance.matchup;

import net.sf.l2j.gameserver.templates.StatsSet;

public class MatchupProfile
{
	private final int attackerClassId;
	private final int targetClassId;
	private final StatsSet modifiers;
	
	public MatchupProfile(StatsSet set)
	{
		attackerClassId = set.getInteger("attacker");
		targetClassId   = set.getInteger("target");
		modifiers = set;
	}
	
	public int getAttacker()
	{
		return attackerClassId;
	}
	
	public int getTarget()
	{
		return targetClassId;
	}
	
	public String getKey()
	{
		return attackerClassId + ":" + targetClassId;
	}
	
	public double getMul(String key)
	{
		return modifiers.getDouble(key, 1.0);
	}
	
	public boolean has(String key)
	{
		return modifiers.containsKey(key);
	}
}
