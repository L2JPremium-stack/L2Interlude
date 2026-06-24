package net.sf.l2j.dailyreward;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.item.kind.Item;

public class DailyReward
{
	private int day;
	private int itemId;
	private int amount;
	private int enchantLevel;
	private Set<Integer> playersReceivdList = new TreeSet<>();
	private Set<String> hwidReceivedList = new TreeSet<>();

	public DailyReward(int day, int itemId)
	{
		this.day = day;
		this.itemId = itemId;
	}

	private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
	static
	{
		suffixes.put(1_000L, "K");
		suffixes.put(1_000_000L, "KK");
		suffixes.put(1_000_000_000L, "KKK");
		suffixes.put(1_000_000_000_000L, "T");
		suffixes.put(1_000_000_000_000_000L, "P");
		suffixes.put(1_000_000_000_000_000_000L, "E");
	}

	
	public String getIcon()
	{
		return getItem().getIcon();
	}

	public String getAmountTxt()
	{
		return format(getAmount());
	}

	public static String format(long value)
	{
		// Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
		if (value == Long.MIN_VALUE)
			return format(Long.MIN_VALUE + 1);
		if (value < 0)
			return "-" + format(-value);
		if (value < 1000)
			return Long.toString(value); // deal with easy case

		Entry<Long, String> e = suffixes.floorEntry(value);
		Long divideBy = e.getKey();
		String suffix = e.getValue();

		long truncated = value / (divideBy / 10); // the number part of the output times 10
		boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
		return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
	}

	public Item getItem()
	{
		return ItemTable.getInstance().getTemplate(itemId);
	}

	/**
	 * @return the itemId
	 */
	public int getItemId()
	{
		return itemId;
	}

	/**
	 * @param itemId the itemId to set
	 */
	public void setItemId(int itemId)
	{
		this.itemId = itemId;
	}

	/**
	 * @return the amount
	 */
	public int getAmount()
	{
		return amount;
	}

	/**
	 * @param amount the amount to set
	 */
	public void setAmount(int amount)
	{
		this.amount = amount;
	}

	/**
	 * @return the enchantLevel
	 */
	public int getEnchantLevel()
	{
		return enchantLevel;
	}

	/**
	 * @param enchantLevel the enchantLevel to set
	 */
	public void setEnchantLevel(int enchantLevel)
	{
		this.enchantLevel = enchantLevel;
	}

	/**
	 * @return the position
	 */
	public int getDay()
	{
		return day;
	}

	/**
	 * @param day 
	 */
	public void setDay(int day)
	{
		this.day = day;
	}

	public Set<Integer> getPlayersReceivdList()
	{
		return playersReceivdList;
	}

	public void setPlayersReceivdList(Set<Integer> playersReceivdList)
	{
		this.playersReceivdList = playersReceivdList;
	}

	public Set<String> getHwidReceivedList() {
		return hwidReceivedList;
	}

	public void setHwidReceivedList(Set<String> hwidReceivedList) {
		this.hwidReceivedList = hwidReceivedList;
	}

}
