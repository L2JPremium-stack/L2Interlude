package net.sf.l2j.gameserver.instancemanager.custom;

public class Roulette
{
	private int _itemId,_enchant,_count,_chance;
	
	public Roulette(int itemId, int enchant, int count, int chance)
	{
		_itemId = itemId;
		_enchant = enchant;
		_count = count;
		_chance = chance;
	}
	
	/**
	 * @return the _itemId
	 */
	public int get_itemId()
	{
		return _itemId;
	}
	/**
	 * @return the _enchant
	 */
	public int get_enchant()
	{
		return _enchant;
	}
	/**
	 * @return the _count
	 */
	public int get_count()
	{
		return _count;
	}
	/**
	 * @return the _chance
	 */
	public int get_chance()
	{
		return _chance;
	}
}