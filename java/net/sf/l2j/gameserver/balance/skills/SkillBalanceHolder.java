package net.sf.l2j.gameserver.balance.skills;

import java.util.HashMap;
import java.util.Map;

public class SkillBalanceHolder
{
	private static final Map<Integer, SkillBalance> SKILLS = new HashMap<>();
	
	public static void add(SkillBalance balance)
	{
		SKILLS.put(balance.getSkillId(), balance);
	}
	
	public static SkillBalance get(int skillId)
	{
		return SKILLS.get(skillId);
	}
	
	public static void clear()
	{
		SKILLS.clear();
	}
	
	public static double getStat(int skillId, String key, double def)
	{
		SkillBalance bal = SKILLS.get(skillId);
		return bal != null ? bal.getDouble(key, def) : def;
	}
	
}
