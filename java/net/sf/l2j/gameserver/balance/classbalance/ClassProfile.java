package net.sf.l2j.gameserver.balance.classbalance;

import net.sf.l2j.gameserver.templates.StatsSet;

public class ClassProfile
{
	private final String classId;
	private final StatsSet stats;
	
	public ClassProfile(StatsSet set)
	{
		classId = set.getString("id");
		stats = set;
	}
	
	public String getClassId()
	{
		return classId;
	}
	
	public double getDouble(String key, double def)
	{
		return stats.getDouble(key, def);
	}
	
	public int getInt(String key, int def)
	{
		return stats.getInteger(key, def);
	}
}
