package net.sf.l2j.bosstimerespawn;

import java.io.File;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.xmlfactory.XMLDocumentFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TimeRaidBossManager
{
	
	private static final Logger _log = Logger.getLogger(TimeRaidBossManager.class.getName());
	private static final Map<Integer, List<RespawnTime>> _bossRespawns = new HashMap<>();
	
	// Classe interna para guardar dados de respawn
	public static class RespawnTime
	{
		private final int dayOfWeek; // 1=Sunday ... 7=Saturday
		private final int hour;
		private final int minute;
		
		public RespawnTime(int dayOfWeek, int hour, int minute)
		{
			if (dayOfWeek < 1 || dayOfWeek > 7)
				throw new IllegalArgumentException("DayOfWeek must be between 1 and 7");
			if (hour < 0 || hour > 23)
				throw new IllegalArgumentException("Hour must be between 0 and 23");
			if (minute < 0 || minute > 59)
				throw new IllegalArgumentException("Minute must be between 0 and 59");
			
			this.dayOfWeek = dayOfWeek;
			this.hour = hour;
			this.minute = minute;
		}
		
		public int getDayOfWeek()
		{
			return dayOfWeek;
		}
		
		public int getHour()
		{
			return hour;
		}
		
		public int getMinute()
		{
			return minute;
		}
		
		@Override
		public String toString()
		{
			return "Day=" + dayOfWeek + " " + String.format("%02d:%02d", hour, minute);
		}
	}
	
	// Singleton
	public static TimeRaidBossManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected TimeRaidBossManager()
	{
		load();
	}
	
	private static void load()
	{
		try
		{
			File file = new File("./data/xml/custom/TimeRaidBoss.xml");
			Document doc = XMLDocumentFactory.getInstance().loadDocument(file);
			Element root = doc.getDocumentElement();
			
			NodeList bosses = root.getElementsByTagName("boss");
			for (int i = 0; i < bosses.getLength(); i++)
			{
				Element bossElement = (Element) bosses.item(i);
				int bossId = Integer.parseInt(bossElement.getAttribute("id"));
				
				NodeList respawns = bossElement.getElementsByTagName("respawn");
				List<RespawnTime> respawnTimes = new ArrayList<>();
				
				for (int j = 0; j < respawns.getLength(); j++)
				{
					Element respawnElement = (Element) respawns.item(j);
					
					String dayStr = respawnElement.getAttribute("day").toUpperCase(); // ex: "FRIDAY"
					int day = convertDayStringToInt(dayStr);
					
					String timeStr = respawnElement.getAttribute("time"); // ex: "21:29"
					String[] parts = timeStr.split(":");
					int hour = Integer.parseInt(parts[0]);
					int minute = Integer.parseInt(parts[1]);
					
					respawnTimes.add(new RespawnTime(day, hour, minute));
				}
				
				_bossRespawns.put(bossId, respawnTimes);
			}
			
			_log.info("TimeRaidBossManager: Loaded respawn times for " + _bossRespawns.size() + " bosses.");
		}
		catch (Exception e)
		{
			_log.warning("TimeRaidBossManager: Failed to load TimeRaidBoss.xml: " + e);
		}
	}
	
	private static int convertDayStringToInt(String dayStr)
	{
		switch (dayStr)
		{
			case "SUNDAY":
				return 1;
			case "MONDAY":
				return 2;
			case "TUESDAY":
				return 3;
			case "WEDNESDAY":
				return 4;
			case "THURSDAY":
				return 5;
			case "FRIDAY":
				return 6;
			case "SATURDAY":
				return 7;
			default:
				throw new IllegalArgumentException("Invalid day of week in XML: " + dayStr);
		}
	}
	
	/**
	 * Retorna a lista de respawns configurados para o boss.
	 * @param bossId
	 * @return
	 */
	public List<RespawnTime> getRespawnTimes(int bossId)
	{
		return _bossRespawns.get(bossId);
	}
	
	/**
	 * Calcula o delay em milissegundos até o próximo respawn configurado para o boss, considerando a lista de horários. Se não houver respawns configurados, retorna -1.
	 * @param bossId
	 * @return
	 */
	public long getMillisUntilNextRespawn(int bossId)
	{
		List<RespawnTime> respawns = getRespawnTimes(bossId);
		if (respawns == null || respawns.isEmpty())
		{
			return -1;
		}
		
		ZonedDateTime now = ZonedDateTime.now();
		long minDelay = Long.MAX_VALUE;
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		
		for (RespawnTime rt : respawns)
		{
			DayOfWeek javaDay = convertToJavaDayOfWeek(rt.getDayOfWeek());
			
			ZonedDateTime nextRespawn = now.with(TemporalAdjusters.nextOrSame(javaDay)).withHour(rt.getHour()).withMinute(rt.getMinute()).withSecond(0).withNano(0);
			
			if (nextRespawn.isBefore(now))
			{
				nextRespawn = nextRespawn.plusWeeks(1);
			}
			
			long delay = Duration.between(now, nextRespawn).toMillis() + 1000; // +1 segundo para evitar spawn antecipado
			
			_log.info("Next respawn candidate for boss " + bossId + ": " + nextRespawn.format(fmt));
			
			if (delay < minDelay)
			{
				minDelay = delay;
			}
		}
		
		return minDelay;
	}
	
	/**
	 * Converte o valor 1..7 do XML para o enum DayOfWeek Java (1=Monday, 7=Sunday). XML usa 1=Sunday...7=Saturday, converter para Java.
	 * @param xmlDay
	 * @return
	 */
	private static DayOfWeek convertToJavaDayOfWeek(int xmlDay)
	{
		switch (xmlDay)
		{
			case 1:
				return DayOfWeek.SUNDAY;
			case 2:
				return DayOfWeek.MONDAY;
			case 3:
				return DayOfWeek.TUESDAY;
			case 4:
				return DayOfWeek.WEDNESDAY;
			case 5:
				return DayOfWeek.THURSDAY;
			case 6:
				return DayOfWeek.FRIDAY;
			case 7:
				return DayOfWeek.SATURDAY;
			default:
				throw new IllegalArgumentException("Invalid day of week: " + xmlDay);
		}
	}
	
	private static class SingletonHolder
	{
		protected static final TimeRaidBossManager _instance = new TimeRaidBossManager();
	}
}
