package net.sf.l2j.gameserver.model.holder;

import net.sf.l2j.gameserver.templates.StatsSet;

public class AugmentStoneHolder
{
	private final String type;
	private final String title;
	private final String icon;
	
	private final int skillId;
	private final int skillLevel;
	
	private final int priceItemId;
	private final int priceCount;
	
	public AugmentStoneHolder(StatsSet set)
	{
		type = set.getString("type");
		title = set.getString("title");
		icon = set.getString("icon", "icon.default");
		
		skillId = set.getInteger("skillId");
		skillLevel = set.getInteger("skillLevel", 1);
		
		priceItemId = set.getInteger("priceItemId", 57);
		priceCount = set.getInteger("priceCount", 0);
	}
	
	public String getType()
	{
		return type;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public String getIcon()
	{
		return icon;
	}
	
	public int getSkillId()
	{
		return skillId;
	}
	
	public int getSkillLevel()
	{
		return skillLevel;
	}
	
	public int getPriceItemId()
	{
		return priceItemId;
	}
	
	public int getPriceCount()
	{
		return priceCount;
	}
	
	public class AugmentStoneHolderBuilder
	{
		private int skillId;
		private int level;
		
		public AugmentStoneHolderBuilder skill(int id, int lvl)
		{
			this.skillId = id;
			this.level = lvl;
			return this;
		}
		
		public AugmentStoneHolder build()
		{
			StatsSet set = new StatsSet();
			set.set("skillId", skillId);
			set.set("skillLevel", level);
			set.set("type", "PASSIVE");
			set.set("title", "Loaded Augment");
			set.set("icon", "icon.skill0000");
			
			return new AugmentStoneHolder(set);
		}
	}
}