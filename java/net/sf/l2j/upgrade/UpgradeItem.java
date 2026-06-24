package net.sf.l2j.upgrade;

import net.sf.l2j.gameserver.templates.StatsSet;

public class UpgradeItem
{
	private final int _itemId;
	private final int _newItemId;
	private final int _chance;
	private final int _bossId;

	public UpgradeItem(StatsSet set)
	{
		_itemId = set.getInteger("itemId");
		_newItemId = set.getInteger("newItemId");
		_chance = set.getInteger("chance");
		_bossId = set.getInteger("bossId");
	}

	public int getItemId() { return _itemId; }
	public int getNewItemId() { return _newItemId; }
	public int getChance() { return _chance; }
	public int getBossId() { return _bossId; }
}
