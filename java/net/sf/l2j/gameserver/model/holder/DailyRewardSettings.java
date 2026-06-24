package net.sf.l2j.gameserver.model.holder;

import net.sf.l2j.gameserver.templates.StatsSet;

public class DailyRewardSettings
{
	private final int _cooldownHours;
	private final int _maxStreak;
	private final boolean _resetOnMissedDay;
	private final int _gracePeriodHours;
	
	public DailyRewardSettings(StatsSet set)
	{
		_cooldownHours = set.getInteger("cooldownHours", 24);
		_maxStreak = set.getInteger("maxStreak", 7);
		_resetOnMissedDay = set.getBool("resetOnMissedDay", false);
		_gracePeriodHours = set.getInteger("gracePeriodHours", 48);
	}
	
	public int getCooldownHours()
	{
		return _cooldownHours;
	}
	
	public int getMaxStreak()
	{
		return _maxStreak;
	}
	
	public boolean isResetOnMissedDay()
	{
		return _resetOnMissedDay;
	}
	
	public int getGracePeriodHours()
	{
		return _gracePeriodHours;
	}
}