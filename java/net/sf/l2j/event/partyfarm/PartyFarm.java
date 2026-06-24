package net.sf.l2j.event.partyfarm;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.util.Broadcast;

public class PartyFarm
{
	public static L2Spawn _monster;
	public static int _bossHeading = 0;
	public static String _eventName = "";
	public static boolean _started = false;
	public static boolean _aborted = false;
	protected static boolean _finish = false;
	static PartyFarm _instance;
	
	public static void bossSpawnMonster()
	{
		if(Config.ENABLE_SPAWN_PARTYFARM_MONSTERS)
		{
			spawnMonsters();
		}
		
		
		Broadcast.gameAnnounceToOnlinePlayers("Teleport Now!");
		Broadcast.gameAnnounceToOnlinePlayers("[Party Farm]: Duration: " + Config.EVENT_BEST_FARM_TIME + " minute(s)!");
		_aborted = false;
		_started = true;
		
		waiter(Config.EVENT_BEST_FARM_TIME * 60 * 1000);
		if (!_aborted)
		{
			Finish_Event();
		}
	}
	public static void Finish_Event()
	{
		if(Config.ENABLE_SPAWN_PARTYFARM_MONSTERS)
		{
			unSpawnMonsters();
		}
		
		_started = false;
		_finish = true;
		
		Broadcast.gameAnnounceToOnlinePlayers("[Party Farm]: Finished!");
		//Broadcast.announceToOnlinePlayers(InitialPartyFarm.getInstance().StartCalculationOfNextEventTime());
		if (!AdminPartyFarm._bestfarm_manual)
		{
			InitialPartyFarm.getInstance().StartCalculationOfNextEventTime();
		}
		else
		{
			AdminPartyFarm._bestfarm_manual = false;
		}
	}
	
	public static void spawnMonsters()
	{
	    int numberOfMonsters = Config.AMOUNT_MONSTROS_SPAWN_PT;
	    int spawnRadius = Config.RADIUS_SPAWN_LOC_PTFARM;
	    int minDistance = 400; // Distância mínima entre monstros aumentada
	    List<int[]> spawnPoints = Config.CUSTOM_SPAWN_PARTYFARM;

	    List<int[]> usedLocations = new ArrayList<>();

	    for (int i = 0; i < numberOfMonsters; i++)
	    {
	        int spawnPointIndex = (int) (Math.random() * spawnPoints.size());
	        int centerX = spawnPoints.get(spawnPointIndex)[0];
	        int centerY = spawnPoints.get(spawnPointIndex)[1];
	        int centerZ = spawnPoints.get(spawnPointIndex)[2];

	        boolean validSpawn = false;
	        int tryCount = 0;
	        final int maxTries = 40;

	        while (!validSpawn && tryCount < maxTries)
	        {
	            double angle = Math.random() * 2 * Math.PI;
	            double distance = Math.sqrt(Math.random()) * spawnRadius;

	            int offsetX = (int) (Math.cos(angle) * distance);
	            int offsetY = (int) (Math.sin(angle) * distance);

	            int xPos = centerX + offsetX;
	            int yPos = centerY + offsetY;
	            int zPos = GeoEngine.getInstance().getHeight(xPos, yPos, centerZ);

	            if (Math.abs(zPos - centerZ) > 150)
	            {
	                tryCount++;
	                continue;
	            }

	            boolean tooClose = false;
	            for (int[] loc : usedLocations)
	            {
	                if (Math.hypot(loc[0] - xPos, loc[1] - yPos) < minDistance)
	                {
	                    tooClose = true;
	                    break;
	                }
	            }

	            if (!tooClose)
	            {
	                monsters.add(spawnNPC(xPos, yPos, zPos, Config.monsterId));
	                usedLocations.add(new int[] {xPos, yPos});
	                validSpawn = true;
	            }

	            tryCount++;
	        }

	        if (!validSpawn)
	        {
	         //   System.out.println("Falha ao spawnar monstro #" + i + ": local inválido após " + maxTries + " tentativas.");
	        }
	    }
	}







	
	public static boolean is_started()
	{
		return _started;
	}
	
	public static boolean is_finish()
	{
		return _finish;
	}
	
	protected static L2Spawn spawnNPC(int xPos, int yPos, int zPos, int npcId)
	{
		NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
		try
		{
			L2Spawn spawn = new L2Spawn(template);
			spawn.setLoc(xPos, yPos, zPos, 0);
			spawn.setRespawnDelay(Config.PARTY_FARM_MONSTER_DALAY);
			
			SpawnTable.getInstance().addNewSpawn(spawn, false);
			
			spawn.setRespawnState(true);
			spawn.doSpawn(false);
			spawn.getNpc().isAggressive();
			spawn.getNpc().decayMe();
			spawn.getNpc().spawnMe(spawn.getNpc().getX(), spawn.getNpc().getY(), spawn.getNpc().getZ());
			spawn.getNpc().broadcastPacket(new MagicSkillUse(spawn.getNpc(), spawn.getNpc(), 1034, 1, 1, 1));
			return spawn;
		}
		catch (Exception e)
		{
		}
		return null;
	}
	
	protected static ArrayList<L2Spawn> monsters = new ArrayList<>();
	
	/*protected static void unSpawnMonsters()
	{
		for (L2Spawn s : monsters)
		{
			if (s == null)
			{
				monsters.remove(s);
				return;
			}

			s.getNpc().deleteMe();
			s.setRespawnState(false);
			SpawnTable.getInstance().deleteSpawn(s, true);
			
		}
	}*/
	protected static void unSpawnMonsters()
	{
	    if (monsters.isEmpty())
	        return;

	    // Criar uma cópia da lista para evitar problemas ao modificar durante a iteração
	    List<L2Spawn> toRemove = new ArrayList<>(monsters);

	    for (L2Spawn spawn : toRemove)
	    {
	        if (spawn == null || spawn.getNpc() == null)
	            continue;

	        spawn.getNpc().deleteMe();
	        spawn.setRespawnState(false);
	        SpawnTable.getInstance().deleteSpawn(spawn, true);
	    }

	    // Limpa tudo de uma vez ao final
	    monsters.clear();
	}

	/*protected static void waiter(long interval)
	{
		long startWaiterTime = System.currentTimeMillis();
		int seconds = (int) (interval / 1000L);
		while ((startWaiterTime + interval > System.currentTimeMillis()) && (!_aborted))
		{
			seconds--;
			switch (seconds)
			{
				case 3600:
					if (_started)
					{
						
						Broadcast.gameAnnounceToOnlinePlayers("[Party Farm]: " + seconds / 60 / 60 + " hour(s) till event finish!");
					}
					break;
				case 60:
				case 120:
				case 180:
				case 240:
				case 300:
				case 600:
				case 900:
				case 1800:
					if (_started)
					{
						
						Broadcast.gameAnnounceToOnlinePlayers("[Party Farm]: " + seconds / 60 + " minute(s) till event finish!");
					}
					break;
				case 1:
				case 2:
				case 3:
				case 10:
				case 15:
				case 30:
					if (_started)
					{
						Broadcast.gameAnnounceToOnlinePlayers("[Party Farm]: " + seconds + " second(s) till event finish!");
					}
					break;
			}
			long startOneSecondWaiterStartTime = System.currentTimeMillis();
			while (startOneSecondWaiterStartTime + 1000L > System.currentTimeMillis())
			{
				try
				{
					Thread.sleep(1L);
				}
				catch (InterruptedException ie)
				{
					if(Config.DEBUG_PATH)
					ie.printStackTrace();
				}
			}
		}
	}*/
	
	
	
	protected static void waiter(long interval)
	{
	    long startWaiterTime = System.currentTimeMillis();
	    int seconds = (int) (interval / 1000L);

	    // Loop principal até o evento terminar ou ser abortado
	    while ((startWaiterTime + interval > System.currentTimeMillis()) && (!_aborted))
	    {
	        seconds--;

	        // Verifica e faz o broadcast conforme o tempo restante
	        if (_started)
	        {
	            // Caso o tempo restante seja uma hora
	            if (seconds == 3600)
	            {
	                Broadcast.gameAnnounceToOnlinePlayers("[Party Farm]: " + seconds / 60 / 60 + " hour(s) till event finish!");
	            }
	            // Caso o tempo restante seja em minutos significativos
	            else if (seconds == 60 || seconds == 120 || seconds == 180 || seconds == 240 || seconds == 300 || seconds == 600 || seconds == 900 || seconds == 1800)
	            {
	                Broadcast.gameAnnounceToOnlinePlayers("[Party Farm]: " + seconds / 60 + " minute(s) till event finish!");
	            }
	            // Caso o tempo restante seja em segundos críticos
	            else if (seconds == 1 || seconds == 2 || seconds == 3 || seconds == 10 || seconds == 15 || seconds == 30)
	            {
	                Broadcast.gameAnnounceToOnlinePlayers("[Party Farm]: " + seconds + " second(s) till event finish!");
	            }
	        }

	        // Espera por 1 segundo antes de continuar o loop
	        try
	        {
	            Thread.sleep(1000L);  // Espera de 1 segundo
	        }
	        catch (InterruptedException ie)
	        {
	            if (Config.DEBUG_PATH)
	                ie.printStackTrace();
	        }
	    }
	}

	
}