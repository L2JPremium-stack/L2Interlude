package net.sf.l2j.gameserver.model.item;

/**
 * Special thanks to nuocnam
 * @author LittleVexy
 */
public class DropData
{
	public static final int MAX_CHANCE = 1000000;
	
	private int _itemId;
	private int _minDrop;
	private int _maxDrop;
	private int _chance;
	//public DropData(int itemId, int minDrop, int maxDrop, int chance)
	//{
	//	_itemId = itemId;
	//	_minDrop = minDrop;
	//	_maxDrop = maxDrop;
	//	_chance = chance;
	//}
	/**
	 * Returns the ID of the item dropped
	 * @return int
	 */
	public int getItemId()
	{
		return _itemId;
	}
	
	/**
	 * Sets the ID of the item dropped
	 * @param itemId : int designating the ID of the item
	 */
	public void setItemId(int itemId)
	{
		_itemId = itemId;
	}
	
	/**
	 * Returns the minimum quantity of items dropped
	 * @return int
	 */
	public int getMinDrop()
	{
		return _minDrop;
	}
	
	/**
	 * Returns the maximum quantity of items dropped
	 * @return int
	 */
	public int getMaxDrop()
	{
		return _maxDrop;
	}
	
	/**
	 * Returns the chance of having a drop
	 * @return int
	 */
	public int getChance()
	{
		return _chance;
	}
	private String _questID = null;
	private String[] _stateID = null;
	/**
	 * Returns the questID.
	 * @return String designating the ID of the quest
	 */
	public String getQuestID()
	{
		return _questID;
	}
	/**
	 * Sets the questID
	 * @param questID String designating the questID to set.
	 */
	public void setQuestID(String questID)
	{
		_questID = questID;
	}
	/**
	 * Returns if the dropped item is requested for a quest
	 * @return boolean
	 */
	public boolean isQuestDrop()
	{
		return _questID != null && _stateID != null;
	}
	/**
	 * Adds states of the dropped item
	 * @param list : String[]
	 */
	public void addStates(String[] list)
	{
		_stateID = list;
	}
	/**
	 * Sets the value for minimal quantity of dropped items
	 * @param mindrop : int designating the quantity
	 */
	public void setMinDrop(int mindrop)
	{
		_minDrop = mindrop;
	}
	
	/**
	 * Sets the value for maximal quantity of dopped items
	 * @param maxdrop : int designating the quantity of dropped items
	 */
	public void setMaxDrop(int maxdrop)
	{
		_maxDrop = maxdrop;
	}
	
	/**
	 * Sets the chance of having the item for a drop
	 * @param chance : int designating the chance
	 */
	public void setChance(int chance)
	{
		_chance = chance;
	}
	
	/**
	 * Returns a report of the object
	 * @return String
	 */
	@Override
	public String toString()
	{
		return "ItemID: " + _itemId + " Min: " + _minDrop + " Max: " + _maxDrop + " Chance: " + (_chance / 10000.0) + "%" + " Enchant: " + _dropEnchant +" Enchant Chance: " + _enchantChance;
	}
	
	/**
	 * Returns if parameter "o" is a L2DropData and has the same itemID that the current object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof DropData)
		{
			DropData drop = (DropData) o;
			return drop.getItemId() == getItemId();
		}
		return false;
	}
	private int _dropEnchant = -1;
	private int _enchantChance = 0;
	public void setEnchant(final int enchant)
	{
		_dropEnchant = enchant;
	}	
	
	public void setEnchantChance(final int chance)
	{
		_enchantChance = chance;
	}
	
	public int getEnchant()
	{
		return _dropEnchant;
	}
	
	public int getEnchantChance()
	{
		return _enchantChance;
	}
}