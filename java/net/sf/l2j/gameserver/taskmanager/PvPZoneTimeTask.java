package net.sf.l2j.gameserver.taskmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.type.L2FlagZone;
import net.sf.l2j.gameserver.util.Broadcast;

public class PvPZoneTimeTask implements Runnable
{
	private List<L2FlagZone> _zones = new ArrayList<>();
	private int _index = 0;
	private L2FlagZone _lastZone = null;
	
	public enum ZoneMode
	{
		SEQUENTIAL,
		RANDOM_NO_REPEAT,
		PURE_RANDOM
	}
	
	private ZoneMode _mode = ZoneMode.SEQUENTIAL;
	
	private L2FlagZone _currentZone;
	private int _timeLeft;
	
	protected PvPZoneTimeTask()
	{
		
		// Se estiver desativado na config, não inicia a task
		if (!Config.ENABLE_PVP_ZONE_TIME)
			return;
		
		Collection<L2FlagZone> zonesCol = ZoneManager.getInstance().getAllZones(L2FlagZone.class);
		if (zonesCol != null)
			_zones = new ArrayList<>(zonesCol);
		
		ThreadPool.scheduleAtFixedRate(this, 1000L * 60, 1000);
	}
	
	@Override
	public void run()
	{
		if (_currentZone == null)
		{
			selectNextZone();
			return;
		}
		
		_timeLeft--;
		
		if (_timeLeft == 300 || _timeLeft == 60 || _timeLeft < 10)
		{
			broadcast("PvP Zone " + _currentZone.getName() + " termina em " + TimeUtil.formatSeconds(_timeLeft) + ".");
		}
		
		if (_timeLeft <= 0)
		{
			endCurrentZone();
			selectNextZone();
		}
	}
	
	private void selectNextZone()
	{
		if (_zones.isEmpty())
			return;
		
		L2FlagZone next = null;
		
		switch (_mode)
		{
			case SEQUENTIAL:
				next = _zones.get(_index++);
				if (_index >= _zones.size())
				{
					_index = 0;
					_mode = ZoneMode.RANDOM_NO_REPEAT;
				}
				break;
			
			case RANDOM_NO_REPEAT:
				do
				{
					next = _zones.get(ThreadLocalRandom.current().nextInt(_zones.size()));
				}
				while (next == _lastZone);
				break;
			
			case PURE_RANDOM:
				next = _zones.get(ThreadLocalRandom.current().nextInt(_zones.size()));
				break;
		}
		
		startZone(next);
	}
	
	private void startZone(L2FlagZone zone)
	{
		_currentZone = zone;
		_lastZone = zone;
		
		zone.setActive(true);
		_timeLeft = zone.getActiveTime();
		
		broadcast("PvP Zone ativada: " + zone.getName() + " por " + TimeUtil.formatSeconds(_timeLeft) + ".");
	}
	
	private void endCurrentZone()
	{
		_currentZone.setActive(false);
		
		for (Creature creature : _currentZone.getCharactersInside())
		{
			if (creature instanceof Player)
			{
				creature.teleToLocation(_currentZone.getRandomReviveLoc(), 75);
			}
		}
		
		broadcast("PvP Zone " + _currentZone.getName() + " finalizada.");
		
		_currentZone = null;
	}
	
	private static void broadcast(String msg)
	{
		Broadcast.announceToOnlinePlayers(msg, true);
	}
	
	public int getTimeLeft()
	{
		return _timeLeft;
	}
	
	public L2FlagZone getCurrentZone()
	{
		return _currentZone;
	}
	
	public static PvPZoneTimeTask getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		private static final PvPZoneTimeTask INSTANCE = new PvPZoneTimeTask();
	}
	
	public final static class TimeUtil
	{
		private TimeUtil()
		{
		}
		
		public static String formatSeconds(int totalSeconds)
		{
			if (totalSeconds < 0)
				totalSeconds = 0;
			
			int hours = totalSeconds / 3600;
			int minutes = (totalSeconds % 3600) / 60;
			int seconds = totalSeconds % 60;
			
			if (hours > 0)
			{
				return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
			}
			
			if (minutes > 0)
			{
				return String.format("%02dm %02ds", minutes, seconds);
			}
			
			return String.format("%02ds", seconds);
		}
	}
	
	public final static class PvPZoneBypass
	{
		private PvPZoneBypass()
		{
		}
		
		public static void handlers(Player player, String command)
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			
			switch (command)
			{
				case "exit":
					PvPZoneTimeTask task = PvPZoneTimeTask.getInstance();
					if (task.getCurrentZone() != null)
						player.teleToLocation(TeleportWhereType.TOWN);
					break;
			}
		}
		
	}
	
}
