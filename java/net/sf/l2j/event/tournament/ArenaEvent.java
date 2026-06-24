package net.sf.l2j.event.tournament;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ThreadPool;

public class ArenaEvent
{
	private static ArenaEvent _instance = null;
	protected static final Logger _log = Logger.getLogger(ArenaEvent.class.getName());
	private Calendar NextEvent;
	private final SimpleDateFormat format = new SimpleDateFormat("HH:mm");
	
	public static ArenaEvent getInstance()
	{
		if (_instance == null)
			_instance = new ArenaEvent();
		return _instance;
	}
	
	public String getNextTime()
	{
		if (getNextEventTime().getTime() != null)
			return format.format(getNextEventTime().getTime());
		return "Erro";
	}
	
	private ArenaEvent()
	{
	}
	
	public void StartCalculationOfNextEventTime()
	{
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar testStartTime = null;
			long flush2 = 0, timeL = 0;
			int count = 0;
			
			for (String timeOfDay : ArenaConfig.TOURNAMENT_EVENT_INTERVAL_BY_TIME_OF_DAY)
			{
				testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);
				String[] splitTimeOfDay = timeOfDay.split(":");
				testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
				testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
				testStartTime.set(Calendar.SECOND, 00);
				if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
				{
					testStartTime.add(Calendar.DAY_OF_MONTH, 1);
				}
				
				timeL = testStartTime.getTimeInMillis() - currentTime.getTimeInMillis();
				
				if (count == 0)
				{
					flush2 = timeL;
					NextEvent = testStartTime;
				}
				
				if (timeL < flush2)
				{
					flush2 = timeL;
					NextEvent = testStartTime;
				}
				count++;
			}
			_log.info("Tournament: Next Event " + NextEvent.getTime().toString());
			ThreadPool.schedule(new StartEventTask(), flush2);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("[Tournament]: "+e);
		}
	}
	
	public Calendar getNextEventTime()
	{
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar testStartTime = null;
			long flush2 = 0, timeL = 0;
			int count = 0;
			Calendar nextEvent = null;
			for (String timeOfDay : ArenaConfig.TOURNAMENT_EVENT_INTERVAL_BY_TIME_OF_DAY)
			{
				testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);
				String[] splitTimeOfDay = timeOfDay.split(":");
				testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
				testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
				testStartTime.set(Calendar.SECOND, 00);
				if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
				{
					testStartTime.add(Calendar.DAY_OF_MONTH, 1);
				}
				
				timeL = testStartTime.getTimeInMillis() - currentTime.getTimeInMillis();
				
				if (count == 0)
				{
					flush2 = timeL;
					nextEvent = testStartTime;
				}
				
				if (timeL < flush2)
				{
					flush2 = timeL;
					nextEvent = testStartTime;
				}
				count++;
			}
			//_log.info("Tournament: Next Event " + nextEvent.getTime().toString());
			return nextEvent;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("[Tournament]: "+e);
			return null;
		}
		
	}
	
	class StartEventTask implements Runnable
	{
		@Override
		public void run()
		{
			_log.info("----------------------------------------------------------------------------");
			_log.info("Tournament: Event Started.");
			_log.info("----------------------------------------------------------------------------");
			ArenaTask.SpawnEvent();
		}
	}
}
