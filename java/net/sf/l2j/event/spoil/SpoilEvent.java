package net.sf.l2j.event.spoil;

import java.util.ArrayList;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.util.Broadcast;

public class SpoilEvent
{
	public static L2Spawn _monster;
	public static int _bossHeading = 0;
	public static String _eventName = "";
	public static boolean _started = false;
	public static boolean _aborted = false;
	protected static boolean _finish = false;
	static SpoilEvent _instance;
	
	public static void bossSpawnMonster()
	{
		spawnMonsters();
		
		spawnGK();
		
		Broadcast.gameAnnounceToOnlinePlayers("Teleport Now!");
		Broadcast.gameAnnounceToOnlinePlayers("[Spoil Event]: Duration: " + Config.EVENT_SPOIL_FARM_TIME + " minute(s)!");
		_aborted = false;
		_started = true;
		
		waiter(Config.EVENT_SPOIL_FARM_TIME * 60 * 1000);
		if (!_aborted)
		{
			Finish_Event();
		}
	}
	public static void Finish_Event()
	{
		unSpawnMonsters();
		
		unSpawnGK();
		
		_started = false;
		_finish = true;
		
		Broadcast.gameAnnounceToOnlinePlayers("[Spoil Event]: Finished!");
		//Broadcast.announceToOnlinePlayers(InitialPartyFarm.getInstance().StartCalculationOfNextEventTime());
		if (!AdminSpoilEvent._bestfarm_manual)
		{
			InitialSpoilEvent.getInstance().StartCalculationOfNextEventTime();
		}
		else
		{
			AdminSpoilEvent._bestfarm_manual = false;
		}
	}
	
	
	//Spawnar GK
	public static void spawnGK()
	{
		for (int i = 0; i < Config.GK_LOCS_COUNT; i++)
		{
			int[] coord = Config.GK_LOCS[i];
			npcs.add(spawnNPCGK(coord[0], coord[1], coord[2], Config.GKNPC_ID));
		}
	}
	
	public static void spawnMonsters()
	{
		for (int i = 0; i < Config.SPOIL_LOCS_COUNT; i++)
		{
			int[] coord = Config.SPOIL_LOCS[i];
			monsters.add(spawnNPC(coord[0], coord[1], coord[2], Config.SPOILMONSTER_ID));
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
			spawn.setRespawnDelay(Config.SPOIL_FARM_MONSTER_DALAY);
			
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
	
	protected static void unSpawnMonsters()
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
	}
	
	protected static ArrayList<L2Spawn> npcs = new ArrayList<>();
	
	protected static void unSpawnGK()
	{
		for (L2Spawn s : npcs)
		{
			if (s == null)
			{
				npcs.remove(s);
				return;
			}

			s.getNpc().deleteMe();
			s.setRespawnState(false);
			SpawnTable.getInstance().deleteSpawn(s, true);
			
		}
	}
	
	protected static L2Spawn spawnNPCGK(int xPos, int yPos, int zPos, int npcId)
	{
		NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
		try
		{
			L2Spawn spawn = new L2Spawn(template);
			spawn.setLoc(xPos, yPos, zPos, 0);
			spawn.setRespawnDelay(10);
			
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
						
						Broadcast.gameAnnounceToOnlinePlayers("[Spoil Event]: " + seconds / 60 / 60 + " hour(s) till event finish!");
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
						
						Broadcast.gameAnnounceToOnlinePlayers("[Spoil Event]: " + seconds / 60 + " minute(s) till event finish!");
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
						Broadcast.gameAnnounceToOnlinePlayers("[Spoil Event]: " + seconds + " second(s) till event finish!");
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
	                Broadcast.gameAnnounceToOnlinePlayers("[Spoil Event]: " + seconds / 60 / 60 + " hour(s) till event finish!");
	            }
	            // Caso o tempo restante seja em minutos significativos
	            else if (seconds == 60 || seconds == 120 || seconds == 180 || seconds == 240 || seconds == 300 || seconds == 600 || seconds == 900 || seconds == 1800)
	            {
	                Broadcast.gameAnnounceToOnlinePlayers("[Spoil Event]: " + seconds / 60 + " minute(s) till event finish!");
	            }
	            // Caso o tempo restante seja em segundos críticos
	            else if (seconds == 1 || seconds == 2 || seconds == 3 || seconds == 10 || seconds == 15 || seconds == 30)
	            {
	                Broadcast.gameAnnounceToOnlinePlayers("[Spoil Event]: " + seconds + " second(s) till event finish!");
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