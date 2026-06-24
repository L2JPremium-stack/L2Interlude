package net.sf.l2j.timezone;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.zone.type.L2TimeFarmZone;
import net.sf.l2j.gameserver.util.Broadcast;

public class TimeFarmZoneManager
{
	private static final List<L2TimeFarmZone> _zones = new ArrayList<>();
	
	private static L2TimeFarmZone _currentZone = null;
	private static L2TimeFarmZone _nextZone = null;
	
	private static ScheduledFuture<?> _rotateTask;
	private static ScheduledFuture<?> _announceTask;
	
	private static long _intervalMs;
	private static long _nextRotateTime;
	
	public static void registerZone(L2TimeFarmZone zone)
	{
		if (zone == null)
			return;
		
		// Só registra a zona se estiver habilitada (enabled == true)
		if (!zone.isEnabled())
			return;
		
		// Adiciona à lista de zonas ativas
		_zones.add(zone);
		
		// Tenta pegar informações da zona (spawns, intervalos, etc)
		TimeFarmZoneData.ZoneInfo zoneInfo = TimeFarmZoneData.getZoneInfo(zone.getZoneName());
		if (zoneInfo == null)
		{
			// Opcional: log se quiser avisar que não encontrou dados para essa zona
			// System.out.println("No zone info found for: '" + zone.getZoneName() + "'");
		}
		else
		{
			// Opcional: log para depuração
			// System.out.println("Found zone info for: '" + zone.getZoneName() + "' with " + zoneInfo.spawns.size() + " spawns");
		}
		
		// Se ainda não temos zona atual definida e a zona atual tem spawns
		if (_currentZone == null && zoneInfo != null && !zoneInfo.spawns.isEmpty())
		{
			_currentZone = zone;
			// System.out.println("TimeFarmZoneManager: Zona inicial definida: " + zone.getZoneName());
			
			int count = 0;
			// Spawn dos mobs da zona inicial
			for (TimeFarmZoneData.MobSpawn spawn : zoneInfo.spawns)
			{
				L2Spawn l2spawn = spawnMob(spawn.mobId, spawn.loc);
				if (l2spawn != null)
				{
					_activeSpawns.add(l2spawn);
					count++;
				}
			}
			
			System.out.println("TimeFarmZoneManager: " + count + " mobs spawnados na zona inicial: " + zone.getZoneName());
			
			// Seleciona próxima zona para rotação
			selectNextZone();
			
			// Agenda início das tarefas (rotacionar, anunciar, etc) com delay de 5s
			ThreadPool.schedule(() -> startTasks(), 5000L);
		}
	}
	
	private static boolean _tasksStarted = false;
	
	public L2TimeFarmZone getZone(Player player)
	{
		for (L2TimeFarmZone zone : getZones())
		{
			if (zone.isInsideZone(player))
				return zone;
		}
		return null;
	}
	
	private static void startTasks()
	{
		if (_zones.isEmpty() || _tasksStarted)
			return;
		
		_tasksStarted = true;
		
		stopTasks();
		
		updateIntervalByCurrentZone();
		
		_nextRotateTime = System.currentTimeMillis() + _intervalMs;
		
		_rotateTask = ThreadPool.scheduleAtFixedRate(() -> {
			rotateZone();
			selectNextZone();
			updateIntervalByCurrentZone();
			_nextRotateTime = System.currentTimeMillis() + _intervalMs;
		}, _intervalMs, _intervalMs);
		
		_announceTask = ThreadPool.scheduleAtFixedRate(TimeFarmZoneManager::announceRemainingTime, 0, 1000L);
	}
	
	public static List<L2TimeFarmZone> getZones()
	{
		List<L2TimeFarmZone> enabledZones = new ArrayList<>();
		for (L2TimeFarmZone zone : _zones)
		{
			if (zone.isEnabled())
			{
				enabledZones.add(zone);
			}
		}
		return enabledZones;
	}
	
	private static void stopTasks()
	{
		if (_rotateTask != null)
		{
			_rotateTask.cancel(false);
			_rotateTask = null;
		}
		
		if (_announceTask != null)
		{
			_announceTask.cancel(false);
			_announceTask = null;
		}
	}
	
	private static void updateIntervalByCurrentZone()
	{
		if (_currentZone == null)
			return;
		
		TimeFarmZoneData.ZoneInfo info = TimeFarmZoneData.getZoneInfo(_currentZone.getZoneName());
		if (info != null)
		{
			_intervalMs = info.intervalMinutes * 60 * 1000L;
		}
		else
		{
			_intervalMs = 10 * 60 * 1000L;
		}
	}
	
	public static void rotateZone()
	{
		if (_nextZone == null || _zones.size() <= 1)
			return;
		
		despawnAllMobs();
		
		@SuppressWarnings("unused")
		L2TimeFarmZone oldZone = _currentZone;
		_currentZone = _nextZone;
		
		// System.out.println("TimeFarmZoneManager: Rotacionando da zona " + oldZone.getZoneName() + " para " + _currentZone.getZoneName());
		
		TimeFarmZoneData.ZoneInfo zoneInfo = TimeFarmZoneData.getZoneInfo(_currentZone.getZoneName());
		if (zoneInfo == null || zoneInfo.spawns.isEmpty())
		{
			System.out.println("TimeFarmZoneManager: Zona " + _currentZone.getZoneName() + " sem spawns definidos no XML!");
			return;
		}
		
		for (TimeFarmZoneData.MobSpawn spawn : zoneInfo.spawns)
		{
			L2Spawn l2spawn = spawnMob(spawn.mobId, spawn.loc);
			if (l2spawn != null)
				_activeSpawns.add(l2spawn);
		}
		
		_nextRotateTime = System.currentTimeMillis() + (zoneInfo.intervalMinutes * 60 * 1000L);
		_intervalMs = zoneInfo.intervalMinutes * 60 * 1000L;
	}
	
	private static void selectNextZone()
	{
		List<L2TimeFarmZone> enabledZones = getZones(); // só habilitadas
		if (enabledZones.size() <= 1)
		{
			_nextZone = _currentZone;
			return;
		}
		
		List<L2TimeFarmZone> possibleZones = new ArrayList<>();
		for (L2TimeFarmZone zone : enabledZones)
		{
			if (zone != _currentZone)
				possibleZones.add(zone);
		}
		
		if (possibleZones.isEmpty())
		{
			_nextZone = _currentZone;
			return;
		}
		
		_nextZone = possibleZones.get((int) (Math.random() * possibleZones.size()));
	}
	
	private static final List<L2Spawn> _activeSpawns = new ArrayList<>();
	
	private static void despawnAllMobs()
	{
		for (L2Spawn spawn : _activeSpawns)
		{
			if (spawn != null && spawn.getNpc() != null)
			{
				spawn.getNpc().deleteMe();
				spawn.setRespawnState(false);
				SpawnTable.getInstance().deleteSpawn(spawn, true);
			}
		}
		_activeSpawns.clear();
	}
	
	private static L2Spawn spawnMob(int mobId, Location loc)
	{
		try
		{
			NpcTemplate template = NpcTable.getInstance().getTemplate(mobId);
			if (template == null)
				return null;
			
			L2Spawn spawn = new L2Spawn(template);
			spawn.setLoc(loc.getX(), loc.getY(), loc.getZ(), 0);
			spawn.setRespawnDelay(10);
			spawn.setRespawnState(true);
			SpawnTable.getInstance().addNewSpawn(spawn, false);
			spawn.doSpawn(false);
			return spawn;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	private static void announceRemainingTime()
	{
		long now = System.currentTimeMillis();
		long remainingMs = _nextRotateTime - now;
		if (remainingMs <= 0)
			return;
		
		int remainingMinutes = (int) (remainingMs / (60 * 1000L));
		int remainingSeconds = (int) ((remainingMs / 1000L) % 60);
		
		int[] announceMinutes =
		{
			60,
			45,
			30,
			15,
			10,
			5,
			4,
			3,
			2,
			1
		};
		int[] announceSeconds =
		{
			30,
			15,
			10,
			5,
			4,
			3,
			2,
			1
		};
		
		boolean shouldAnnounce = false;
		
		for (int m : announceMinutes)
		{
			if (remainingMinutes == m && remainingSeconds == 0)
			{
				shouldAnnounce = true;
				break;
			}
		}
		
		if (!shouldAnnounce && remainingMinutes == 0)
		{
			for (int s : announceSeconds)
			{
				if (remainingSeconds == s)
				{
					shouldAnnounce = true;
					break;
				}
			}
		}
		
		if (shouldAnnounce)
		{
			String timeText = (remainingMinutes > 0) ? remainingMinutes + " minute(s)" : remainingSeconds + " second(s)";
			
			String zoneName = _nextZone.getZoneName();
			Broadcast.gameAnnounceToOnlinePlayers("[FarmZone] Next move to Zone " + zoneName + " in " + timeText + ". Get ready!");
		}
		
	}
	
	public static void init()
	{
		List<L2TimeFarmZone> enabledZones = getZones();
		if (enabledZones.isEmpty())
		{
			System.out.println("TimeFarmZoneManager: Nenhuma zona habilitada para iniciar!");
			return;
		}
		
		_currentZone = enabledZones.get(0);
		System.out.println("TimeFarmZoneManager: Zona inicial definida: " + _currentZone.getZoneName());
		
		// Spawn inicial dos mobs da zona
		TimeFarmZoneData.ZoneInfo zoneInfo = TimeFarmZoneData.getZoneInfo(_currentZone.getZoneName());
		if (zoneInfo != null && !zoneInfo.spawns.isEmpty())
		{
			for (TimeFarmZoneData.MobSpawn spawn : zoneInfo.spawns)
			{
				L2Spawn l2spawn = spawnMob(spawn.mobId, spawn.loc);
				if (l2spawn != null)
					_activeSpawns.add(l2spawn);
			}
			System.out.println("TimeFarmZoneManager: " + _activeSpawns.size() + " mobs spawnados na zona inicial: " + _currentZone.getZoneName());
		}
		else
		{
			System.out.println("TimeFarmZoneManager: Nenhum mob definido para a zona inicial: " + _currentZone.getZoneName());
		}
		
		selectNextZone();
		
		startTasks();
	}
	
	public static L2TimeFarmZone getCurrentZone()
	{
		return _currentZone;
	}
	
}
