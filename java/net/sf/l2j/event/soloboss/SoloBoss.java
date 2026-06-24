package net.sf.l2j.event.soloboss;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.event.bossevent.KTBEvent;
import net.sf.l2j.event.deathmath.DMEvent;
import net.sf.l2j.event.fortress.FOSEvent;
import net.sf.l2j.event.lastman.LMEvent;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.SpawnLocation;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ConfirmDlg;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.util.Broadcast;

public class SoloBoss
{
	public static L2Spawn _monster;
	public static int _bossHeading = 0;
	public static String _eventName = "";
	public static boolean _started = false;
	public static boolean _aborted = false;
	public static boolean _finish = false;
	static SoloBoss _instance;
	public static boolean boss_teleporter = false;
	public static void bossSpawnMonster()
	{
		if(Config.SCREN_MSG_BOSS_SOLO)
		{
			InviteForSpawnBoss();
		}
		SpawnAllRaids();  // Isso spawnará todos os bosses automaticamente.
		Broadcast.gameAnnounceToOnlinePlayers("Teleport Now!");
		Broadcast.gameAnnounceToOnlinePlayers("[Solo Boss]: Duration: " + Config.EVENT_SOLOBOSS_TIME + " minute(s)!");
		_aborted = false;
		_started = true;
		
		waiter(Config.EVENT_SOLOBOSS_TIME * 60 * 1000);
		if (!_aborted)
		{
			Finish_Event();
		}
	}
	
	public static void Finish_Event()
	{
		unspawnNpcNPCS();

		_started = false;
		_finish = true;
		_aborted = true;
		
		Broadcast.gameAnnounceToOnlinePlayers("[Solo Boss]: Finished!");
		Broadcast.gameAnnounceToAll("Next Solo Boss Event: " + InitialSoloBossEvent.getInstance().getNextTime() + " (GMT-3)."); // 8D
		//Broadcast.announceToOnlinePlayers(InitialPartyFarm.getInstance().StartCalculationOfNextEventTime());
		if (!AdminSoloBoss._bestfarm_manual)
		{
			InitialSoloBossEvent.getInstance().StartCalculationOfNextEventTime();
		}
		else
		{
			AdminSoloBoss._bestfarm_manual = false;
		}
	}
	private static void InviteForSpawnBoss()
	{
		boss_teleporter = true;

		for (Player player : L2World.getInstance().getPlayers())
		{
			
			if (player != null && player.isOnline())
			{

				ThreadPool.schedule(new Runnable()
				{
					@Override
					public void run()
					{
						boss_teleporter = false;
					}
				}, 46000);
				
				if (Config.SCREN_MSG_BOSS_SOLO)
				{
					if (player.inObserverMode() || player.isDead() || FOSEvent.isPlayerParticipant(player.getObjectId()) && FOSEvent.isStarted() || DMEvent.isPlayerParticipant(player.getObjectId()) && DMEvent.isStarted() || KTBEvent.isPlayerParticipant(player.getObjectId()) && KTBEvent.isStarted() || LMEvent.isPlayerParticipant(player.getObjectId()) && LMEvent.isStarted() || player.isAlikeDead() || player.isInArenaEvent() || player.isArenaProtection() || player.isOlympiadProtection() || player.isInStoreMode() || player.isRooted() || player.getKarma() > 0 || player.isInOlympiadMode() || player.isOff() || player.isOffShop() || player.isFestivalParticipant() || player.isArenaAttack() || player.isInsideZone(ZoneId.BOSS) || player.isInsideZone(ZoneId.FLAG_AREA_BOSS) || player.isInsideZone(ZoneId.ARENA_EVENT) || player.isInsideZone(ZoneId.PVP_CUSTOM) || player.isInsideZone(ZoneId.RAID_ZONE) || player.isInsideZone(ZoneId.SIEGE)  || player.isInsideZone(ZoneId.SPOIL_AREA))
					{
						continue;
					}
					SpawnLocation _position = new SpawnLocation(Config.SOLO_BOSS_ID_ONE_LOC[0] + Rnd.get(-300, 300), Config.SOLO_BOSS_ID_ONE_LOC[1] + Rnd.get(-100, 100), Config.SOLO_BOSS_ID_ONE_LOC[2], 0);

					ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.S1.getId());
					confirm.addString("Let's go to Solo Boss?");
				//	confirm.addZoneName(_position);
					confirm.addZoneName(_position.getX(), _position.getY(), _position.getZ());
					confirm.addTime(45000);
					confirm.addRequesterId(player.getObjectId());
					player.sendPacket(confirm);
				}
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
	public static void SpawnAllRaids()
	{
	    // Chama apenas o spawn do primeiro Boss
	    SpawnRaid(Config.SOLO_BOSS_ID_ONE, Config.SOLO_BOSS_ID_ONE_LOC);
	}
	public static L2Spawn _npcSpawnActive;  // Armazena o spawn do boss ativo

	public static void SpawnRaid(int bossId, int[] bossLoc)
	{
	    NpcTemplate tmpl = NpcTable.getInstance().getTemplate(bossId);
	    
	    try
	    {
	        L2Spawn npcSpawn = new L2Spawn(tmpl);
	        npcSpawn.setLoc(bossLoc[0], bossLoc[1], bossLoc[2], 0);
	        npcSpawn.setRespawnDelay(10);
	        _npcSpawnActive = npcSpawn;
	        SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
	        npcSpawn.setRespawnState(false);
	        npcSpawn.doSpawn(true);
	        npcSpawn.getNpc().isAggressive();
	        npcSpawn.getNpc().decayMe();
	        npcSpawn.getNpc().spawnMe(npcSpawn.getNpc().getX(), npcSpawn.getNpc().getY(), npcSpawn.getNpc().getZ());
	        npcSpawn.getNpc().broadcastPacket(new MagicSkillUse(npcSpawn.getNpc(), npcSpawn.getNpc(), 1034, 1, 1, 1));
	        Broadcast.gameAnnounceToOnlinePlayers("Solo Boss: " + NpcTable.getInstance().getTemplate(bossId).getName() + " was spawned in the world!");
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }
	}
	public static L2Spawn _npcSpawn1;
	
	public static void unspawnNpcNPCS()
	{
	    // Despawns para o GK (Spawn 1) e o Boss (Spawn 2)
	    if (_npcSpawn1 != null && _npcSpawn1.getNpc() != null)
	    {
	        _npcSpawn1.getNpc().deleteMe();  // Remove o GK da cena
	        _npcSpawn1.setRespawnState(false);  // Desativa o respawn do GK
	        SpawnTable.getInstance().deleteSpawn(_npcSpawn1, true);  // Remove o spawn do GK da tabela
	    }

	    // Despawns o Boss ativo
	    if (_npcSpawnActive != null && _npcSpawnActive.getNpc() != null)
	    {
	        _npcSpawnActive.getNpc().deleteMe();  // Remove o boss da cena
	        _npcSpawnActive.setRespawnState(false);  // Desativa o respawn do Boss
	        SpawnTable.getInstance().deleteSpawn(_npcSpawnActive, true);  // Remove o spawn do Boss da tabela
	    }
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
						
						Broadcast.gameAnnounceToOnlinePlayers("[Solo Boss]: " + seconds / 60 / 60 + " hour(s) till event finish!");
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
						
						Broadcast.gameAnnounceToOnlinePlayers("[Solo Boss]: " + seconds / 60 + " minute(s) till event finish!");
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
						Broadcast.gameAnnounceToOnlinePlayers("[Solo Boss]: " + seconds + " second(s) till event finish!");
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
	    
	    while ((startWaiterTime + interval > System.currentTimeMillis()) && (!_aborted))
	    {
	        seconds--;

	        // Verifica o tempo restante e faz o broadcast conforme necessário
	        if (_started)
	        {
	            if (seconds == 3600) { // 1 hora restante
	                Broadcast.gameAnnounceToOnlinePlayers("[Solo Boss]: " + (seconds / 60 / 60) + " hour(s) till event finish!");
	            }
	            else if (seconds == 60 || seconds == 120 || seconds == 180 || seconds == 240 || seconds == 300 || seconds == 600 || seconds == 900 || seconds == 1800) {
	                Broadcast.gameAnnounceToOnlinePlayers("[Solo Boss]: " + (seconds / 60) + " minute(s) till event finish!");
	            }
	            else if (seconds == 1 || seconds == 2 || seconds == 3 || seconds == 10 || seconds == 15 || seconds == 30) {
	                Broadcast.gameAnnounceToOnlinePlayers("[Solo Boss]: " + seconds + " second(s) till event finish!");
	            }
	        }

	        // Espera 1 segundo (1000ms) antes de continuar o loop
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