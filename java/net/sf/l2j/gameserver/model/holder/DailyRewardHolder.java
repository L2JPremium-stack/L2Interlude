package net.sf.l2j.gameserver.model.holder;

import net.sf.l2j.gameserver.templates.StatsSet;

public class DailyRewardHolder
{
	private final int _day;
	private final int _itemId;
	private final long _count;
	private final int _enchant;
	private final String _sound;
	private final int _playTime;
	private final int _monsterKills;
	
	public DailyRewardHolder(StatsSet set)
	{
		_day = set.getInteger("value");
		_itemId = set.getInteger("itemId");
		_count = set.getLong("count");
		_enchant = set.getInteger("enchant", 0);
		_sound = set.getString("sound", "");
		_playTime = set.getInteger("playTimeMinutes", 0);
		_monsterKills = set.getInteger("monsterKills", 0);
	}
	
	public int getDay()
	{
		return _day;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public long getCount()
	{
		return _count;
	}
	
	public int getEnchant()
	{
		return _enchant;
	}
	
	public String getSound()
	{
		return _sound;
	}
	
	public int getPlayTime()
	{
		return _playTime;
	}
	
	public int getMonsterKills()
	{
		return _monsterKills;
	}
}