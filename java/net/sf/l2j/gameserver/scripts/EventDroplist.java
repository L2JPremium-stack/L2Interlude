package net.sf.l2j.gameserver.scripts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class EventDroplist
{
	// private static Logger _log = Logger.getLogger(EventDroplist.class.getName());
	
	private static EventDroplist _instance;
	
	/** The table containing all DataDrop object */
	private List<DateDrop> _allNpcDateDrops;
	
	public static EventDroplist getInstance()
	{
		if (_instance == null)
		{
			_instance = new EventDroplist();
		}
		
		return _instance;
	}
	
	public class DateDrop
	{
		/** Start and end date of the Event */
		public DateRange dateRange;
		
		/** The table containing Item identifier that can be dropped as extra Items during the Event */
		public int[] items;
		
		/** The min number of Item dropped in one time during this Event */
		public int min;
		
		/** The max number of Item dropped in one time during this Event */
		public int max;
		
		/** The rate of drop for this Event */
		public int chance;
	}
	
	/**
	 * Constructor of EventDroplist.<BR>
	 * <BR>
	 */
	private EventDroplist()
	{
		_allNpcDateDrops = new ArrayList<>();
	}
	
	/**
	 * Create and Init a new DateDrop then add it to the allNpcDateDrops of EventDroplist .<BR>
	 * <BR>
	 * @param items The table containing all item identifier of this DateDrop
	 * @param count The table containing min and max value of this DateDrop
	 * @param chance The chance to obtain this drop
	 * @param range The DateRange object to add to this DateDrop
	 */
	public void addGlobalDrop(int[] items, int[] count, int chance, DateRange range)
	{
		
		DateDrop date = new DateDrop();
		
		date.dateRange = range;
		date.items = items;
		date.min = count[0];
		date.max = count[1];
		date.chance = chance;
		
		_allNpcDateDrops.add(date);
		date = null;
	}
	
	public List<DateDrop> getAllDrops()
	{
		Date currentDate = new Date();
		
		return _allNpcDateDrops.stream().filter(drop -> drop.dateRange.isWithinRange(currentDate)).collect(Collectors.toList());
	}
	
}