package net.sf.l2j.gameserver.balance.skills;

import net.sf.l2j.gameserver.templates.StatsSet;

public class SkillBalance
{
	private final int skillId;
	private final StatsSet stats;
	
	public SkillBalance(StatsSet set)
	{
		skillId = set.getInteger("id");
		stats = set;
	}
	
	public int getSkillId()
	{
		return skillId;
	}
	
	public double getDouble(String key, double def)
	{
		return stats.getDouble(key, def);
	}
}
