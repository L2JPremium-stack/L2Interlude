package net.sf.l2j.event.tournament;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.util.Broadcast;


public class Arena1x1 implements Runnable
{
	// list of participants
	public static List<Pair> registered;
	// number of Arenas
	int free = ArenaConfig.ARENA_EVENT_COUNT_1X1;
	// Arenas
	Arena[] arenas = new Arena[ArenaConfig.ARENA_EVENT_COUNT_1X1];
	// list of fights going on
	Map<Integer, String> fights = new HashMap<>(ArenaConfig.ARENA_EVENT_COUNT_1X1);

	public Arena1x1()
	{
		registered = new ArrayList<>();
		int[] coord;
		for (int i = 0; i < ArenaConfig.ARENA_EVENT_COUNT_1X1; i++)
		{
			coord = ArenaConfig.ARENA_EVENT_LOCS_1X1[i];
			arenas[i] = new Arena(i, coord[0], coord[1], coord[2]);
		}
	}

	public static Arena1x1 getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	public boolean register(Player player)
	{
		for (Pair p : registered)
		{
			if (p.getLeader() == player)
			{
				player.sendMessage("Tournament: You already registered!");
				return false;
			}
		}
		return registered.add(new Pair(player));
	}

	public boolean isRegistered(Player player)
	{
		for (Pair p : registered)
		{
			if (p.getLeader() == player)
			{
				return true;
			}
		}
		return false;
	}

	public Map<Integer, String> getFights()
	{
		return fights;
	}

	public boolean remove(Player player)
	{
		for (Pair p : registered)
		{
			if (p.getLeader() == player)
			{
				p.removeMessage();
				registered.remove(p);
				return true;
			}
		}
		return false;
	}

	@Override
	public synchronized void run()
	{
		boolean load = true;

		// while server is running
		while (load)
		{
			if (!ArenaTask.is_started())
				load = false;

			// if no have participants or arenas are busy wait 1 minute
		//	if (registered.size() < 2 || free == 0)
			if (registered.size() < 2)
			{
				try
				{
					Thread.sleep(ArenaConfig.ARENA_CALL_INTERVAL * 1000);
				}
				catch (InterruptedException e)
				{
				}
				continue;
			}
			List<Pair> opponents = selectOpponents();
			if (opponents != null && opponents.size() == 2)
			{
				Thread T = new Thread(new EvtArenaTask(opponents));
				T.setDaemon(true);
				T.start();
			}
			// wait 1 minute for not stress server
			try
			{
				Thread.sleep(ArenaConfig.ARENA_CALL_INTERVAL * 1000);
			}
			catch (InterruptedException e)
			{
			}
		}
	}

	@SuppressWarnings("null")
	private List<Pair> selectOpponents()
	{
		List<Pair> opponents = new ArrayList<>();
		Pair pairOne = null, pairTwo = null;
		int tries = 3;
		do
		{
			int first = 0, second = 0;
			if (getRegisteredCount() < 2)
				return opponents;

			if (pairOne == null)
			{
				first = Rnd.get(getRegisteredCount());
				pairOne = registered.get(first);
				if (pairOne.check())
				{
					opponents.add(0, pairOne);
					registered.remove(first);
				}
				else
				{
					pairOne = null;
					registered.remove(first);
					return null;
				}

			}
			if (pairTwo == null)
			{
				second = Rnd.get(getRegisteredCount());
				pairTwo = registered.get(second);
				if (pairTwo.check())
				{
					opponents.add(1, pairTwo);
					registered.remove(second);
				}
				else
				{
					pairTwo = null;
					registered.remove(second);
					return null;
				}

			}
		}
		while ((pairOne == null || pairTwo == null) && --tries > 0);
		return opponents;
	}

	public void clear()
	{
		registered.clear();
	}

	public int getRegisteredCount()
	{
		return registered.size();
	}

	private class Pair
	{
		private Player leader;

		public Pair(Player leader)
		{
			this.leader = leader;
		}

		public Player getLeader()
		{
			return leader;
		}

		public boolean check()
		{
			if ((leader == null || !leader.isOnline()))
				return false;

			return true;
		}

		public boolean isDead()
		{
			if (ArenaConfig.ARENA_PROTECT)
			{
				if (leader != null && leader.isOnline() && leader.isArenaAttack() && !leader.isDead() && !leader.isInsideZone(ZoneId.ARENA_EVENT))
					leader.logout(true);
			}

			if ((leader == null || leader.isDead() || !leader.isOnline() || !leader.isInsideZone(ZoneId.ARENA_EVENT) || !leader.isArenaAttack()))
				return false;

			return !(leader.isDead());
		}

		public boolean isAlive()
		{
			if ((leader == null || leader.isDead() || !leader.isOnline() || !leader.isInsideZone(ZoneId.ARENA_EVENT) || !leader.isArenaAttack()))
				return false;

			return !(leader.isDead());
		}

		public void teleportTo(int x, int y, int z)
		{
			if (leader != null && leader.isOnline())
			{
			//	leader.getAppearance().setInvisible();
				leader.setCurrentCp(leader.getMaxCp());
				leader.setCurrentHp(leader.getMaxHp());
				leader.setCurrentMp(leader.getMaxMp());
				leader.setTeamTour1(false);
				leader.setTeamTour2(false);
				/*
				if (leader.isInObserverMode())
				{
					leader.setLastCords(x, y, z);
					leader.leaveOlympiadObserverMode();
				}
				 */
				if (!leader.isInJail())
					leader.teleToLocation(x, y, z, 0);

				leader.broadcastUserInfo();
			}
		}

//		public void EventTitle(String title, String color)
//		{
//			if (leader != null && leader.isOnline())
//			{
//				leader.setTitle(title);
//				leader.getAppearance().setTitleColor(Integer.decode("0x" + color));
//				leader.broadcastUserInfo();
//				leader.broadcastTitleInfo();
//			}
//		}
		public void EventAura(int title)
		{
			if (leader != null && leader.isOnline())
			{
				//leader.setTitle(title);
			//	leader.getAppearance().setTitleColor(Integer.decode("0x" + color));
			//	leader.isTeamTour1();
				leader.setTeamTour1(true);
				leader.broadcastUserInfo();
			//	leader.broadcastCharInfo();
			//	leader.broadcastTitleInfo();
			}
		}
		public void EventAura2(int title)
		{
			if (leader != null && leader.isOnline())
			{
				//leader.setTitle(title);
			//	leader.getAppearance().setTitleColor(Integer.decode("0x" + color));
			//	leader.isTeamTour2();
				leader.setTeamTour2(true);
				leader.broadcastUserInfo();
			//	leader.broadcastCharInfo();
			//	leader.broadcastTitleInfo();
			}
		}
		public void saveTitle()
		{
			if (leader != null && leader.isOnline())
			{
				leader._originalTitleColorTournament = leader.getAppearance().getTitleColor();
				leader._originalTitleTournament = leader.getTitle();
			}
		}

		public void backTitle()
		{
			if (leader != null && leader.isOnline())
			{
				leader.setTitle(leader._originalTitleTournament);
				leader.getAppearance().setTitleColor(leader._originalTitleColorTournament);
				leader.broadcastUserInfo();
				leader.broadcastTitleInfo();
			}
		}

		public void setArenaInstance(InstanceHolder instance) 
		{
		
			if (leader != null && leader.isOnline())
				leader.setInstance(instance, true); //1x1 Tournament Instance
		}

		public void setRealInstance() 
		{
			InstanceHolder instance = InstanceManager.getInstance().getInstance(0);
			if (leader != null && leader.isOnline())
				leader.setInstance(instance, true);
		}

		public void rewards()
		{
			
			if (leader != null && leader.isOnline())
			{
				if (leader.isVip())
					leader.addItem("Arena_Event", ArenaConfig.ARENA_REWARD_ID, (int) (ArenaConfig.ARENA_WIN_REWARD_COUNT_1X1 * Config.VIP_DROP_RATE), leader, true);
				else
					leader.addItem("Arena_Event", ArenaConfig.ARENA_REWARD_ID, ArenaConfig.ARENA_WIN_REWARD_COUNT_1X1, leader, true);
				
				if (Config.ACTIVE_MISSION_1X1)
				{
					if (!this.leader.check_obj_mission(this.leader.getObjectId()))
						this.leader.updateMission(); 
					if (!this.leader.is1x1Completed() && this.leader.getTournament1x1Cont() < Config.MISSION_1X1_CONT)
						this.leader.setTournament1x1Cont(this.leader.getTournament1x1Cont() + 1); 
				}
			}

			sendPacket("Congratulations, your team won the event!", 5);
		}

		public void rewardsLost()
		{
			if (leader != null && leader.isOnline())
			{
				if (leader.isVip())
					leader.addItem("Arena_Event", ArenaConfig.ARENA_REWARD_ID, (int) (ArenaConfig.ARENA_LOST_REWARD_COUNT_1X1 * Config.VIP_DROP_RATE), leader, true);
				else
					leader.addItem("Arena_Event", ArenaConfig.ARENA_REWARD_ID, ArenaConfig.ARENA_LOST_REWARD_COUNT_1X1, leader, true);
			}

			sendPacket("your team lost the event! =(", 5);
		}

		public void setInTournamentEvent(boolean val)
		{
			if (leader != null && leader.isOnline())
				leader.setInArenaEvent(val);
		}

		public void removeMessage()
		{
			if (leader != null && leader.isOnline())
			{
				leader.sendMessage("Tournament: Your participation has been removed.");
				leader.setArenaProtection(false);
				leader.setArena1x1(false);
			}
		}

		public void setArenaProtection(boolean val)
		{
			if (leader != null && leader.isOnline())
			{
				leader.setArenaProtection(val);
				leader.setArena1x1(val);
			}
		}

		public void revive()
		{
			if (leader != null && leader.isOnline() && leader.isDead())
				leader.doRevive();
		}

		public void setImobilised(boolean val)
		{
			if (leader != null && leader.isOnline())
			{
				leader.setIsInvul(val);
				leader.setStopArena(val);
			}
		}

		public void setArenaAttack(boolean val)
		{
			if (leader != null && leader.isOnline())
			{
				leader.setArenaAttack(val);
				leader.broadcastUserInfo();
			}
		}

		public void removePet()
		{
			if (leader != null && leader.isOnline())
			{
				// Remove Summon's buffs
				if (leader.getPet() != null)
				{
					L2Summon summon = leader.getPet();
					if (summon != null)
						summon.unSummon(summon.getOwner());

					if (summon instanceof L2PetInstance)
						summon.unSummon(leader);
				}

				if (leader.getMountType() == 1 || leader.getMountType() == 2)
					leader.dismount();
			}
		}

		public void removeSkills()
		{
			if (!(leader.getClassId() == ClassId.SHILLIEN_ELDER || leader.getClassId() == ClassId.SHILLIEN_SAINT || leader.getClassId() == ClassId.BISHOP || leader.getClassId() == ClassId.CARDINAL || leader.getClassId() == ClassId.ELVEN_ELDER || leader.getClassId() == ClassId.EVAS_SAINT))
			{
				for (L2Effect effect : leader.getAllEffects())
				{
					if (ArenaConfig.ARENA_STOP_SKILL_LIST.contains(effect.getSkill().getId()))
						leader.stopSkillEffects(effect.getSkill().getId());
				}
			}
		}

		public void sendPacket(String message, int duration)
		{
			if (leader != null && leader.isOnline())
				leader.sendPacket(new ExShowScreenMessage(message, duration * 1000));
		}

		public void inicarContagem(int duration)
		{
			if (leader != null && leader.isOnline())
				ThreadPool.schedule(new countdown(leader, duration), 0);
		}

		public void sendPacketinit(String message, int duration)
		{
			if (leader != null && leader.isOnline())
				leader.sendPacket(new ExShowScreenMessage(message, duration * 1000, ExShowScreenMessage.SMPOS.MIDDLE_LEFT, false));
		}
	}

	private class EvtArenaTask implements Runnable
	{
		private final Pair pairOne;
		private final Pair pairTwo;
		private final int pOneX, pOneY, pOneZ, pTwoX, pTwoY, pTwoZ;
		private Arena arena;

		public EvtArenaTask(List<Pair> opponents)
		{
			pairOne = opponents.get(0);
			pairTwo = opponents.get(1);
			Player leader = pairOne.getLeader();
			pOneX = leader.getX();
			pOneY = leader.getY();
			pOneZ = leader.getZ();
			leader = pairTwo.getLeader();
			pTwoX = leader.getX();
			pTwoY = leader.getY();
			pTwoZ = leader.getZ();
		}

		@Override
		public void run()
		{
			InstanceHolder instance = InstanceManager.getInstance().createInstance();
	//		free--;
			pairOne.saveTitle();
			pairTwo.saveTitle();
			portPairsToArena(instance);
			pairOne.inicarContagem(ArenaConfig.ARENA_WAIT_INTERVAL_1X1);
			pairTwo.inicarContagem(ArenaConfig.ARENA_WAIT_INTERVAL_1X1);
			try
			{
				Thread.sleep(ArenaConfig.ARENA_WAIT_INTERVAL_1X1 * 1000);
			}
			catch (InterruptedException e1)
			{
			}
			pairOne.sendPacketinit("Started. Good Fight!", 3);
			pairTwo.sendPacketinit("Started. Good Fight!", 3);
	//		pairOne.EventTitle(ArenaConfig.MSG_TEAM1, ArenaConfig.TITLE_COLOR_TEAM1);
	//		pairTwo.EventTitle(ArenaConfig.MSG_TEAM2, ArenaConfig.TITLE_COLOR_TEAM2);
			pairOne.EventAura(ArenaConfig.AURA_COLOR_TEAM1);
			pairTwo.EventAura2(ArenaConfig.AURA_COLOR_TEAM2);
			pairOne.setImobilised(false);
			pairTwo.setImobilised(false);
			pairOne.setArenaAttack(true);
			pairTwo.setArenaAttack(true);

			while (check())
			{
				// check players status each seconds
				try
				{
					Thread.sleep(ArenaConfig.ARENA_CHECK_INTERVAL);
				}
				catch (InterruptedException e)
				{
					break;
				}
			}
			finishDuel();
		//	free++;
		}

		private void finishDuel()
		{
			fights.remove(arena.id);
			rewardWinner();
			pairOne.revive();
			pairTwo.revive();
			pairOne.teleportTo(pOneX, pOneY, pOneZ);
			pairTwo.teleportTo(pTwoX, pTwoY, pTwoZ);
			pairOne.backTitle();
			pairTwo.backTitle();
			pairOne.setRealInstance();
			pairTwo.setRealInstance();
			pairOne.setInTournamentEvent(false);
			pairTwo.setInTournamentEvent(false);
			pairOne.setArenaProtection(false);
			pairTwo.setArenaProtection(false);
			pairOne.setArenaAttack(false);
			pairTwo.setArenaAttack(false);
			//arena.setFree(true);
		}

		private void rewardWinner()
		{
			Player leader1 = pairOne.getLeader();
			Player leader2 = pairTwo.getLeader();

			if (pairOne.isAlive() && !pairTwo.isAlive())
			{
				if (ArenaConfig.TOURNAMENT_EVENT_ANNOUNCE)
					Broadcast.gameAnnounceToOnlinePlayers("[ 1X1 ]: [" + leader1.getName() + " VS " + leader2.getName() + "] ~> " + leader1.getName() + " WIN!");

				pairOne.rewards();
				pairTwo.rewardsLost();
			}
			
			if (pairTwo.isAlive() && !pairOne.isAlive())
			{
				if (ArenaConfig.TOURNAMENT_EVENT_ANNOUNCE)
					Broadcast.gameAnnounceToOnlinePlayers("[ 1X1 ]: [" + leader1.getName() + " VS " + leader2.getName() + "] ~> " + leader2.getName() + " WIN!");
				
				pairTwo.rewards();
				pairOne.rewardsLost();
			}
		}

		private boolean check()
		{
			return (pairOne.isDead() && pairTwo.isDead());
		}

		/*private void portPairsToArena(Instance instance)
		{
		
					int id=1;
					int[] coord = ArenaConfig.ARENA_EVENT_LOCS_1X1[0];
					Arena arena = new Arena(id, coord[0], coord[1], coord[2]);
					pairOne.removePet();
					pairTwo.removePet();
					pairOne.setArenaInstance(instance);
					pairTwo.setArenaInstance(instance);
					pairOne.teleportTo(arena.x - 850, arena.y, arena.z);
					pairTwo.teleportTo(arena.x + 850, arena.y, arena.z);
					pairOne.setImobilised(true);
					pairTwo.setImobilised(true);
					pairOne.setInTournamentEvent(true);
					pairTwo.setInTournamentEvent(true);
					pairOne.removeSkills();
					pairTwo.removeSkills();
					//fights.put(this.arena.id, pairOne.getLeader().getName() + " vs " + pairTwo.getLeader().getName());
					
		}
		
		@SuppressWarnings("unused")
		private void portPairsToArena1(Instance instance)
		{
			for (Arena arena : arenas)
			{
				if (arena.isFree)
				{
					this.arena = arena;
					arena.setFree(false);
					pairOne.removePet();
					pairTwo.removePet();
					pairOne.setArenaInstance(instance);
					pairTwo.setArenaInstance(instance);
					pairOne.teleportTo(arena.x - 850, arena.y, arena.z);
					pairTwo.teleportTo(arena.x + 850, arena.y, arena.z);
					pairOne.setImobilised(true);
					pairTwo.setImobilised(true);
					pairOne.setInTournamentEvent(true);
					pairTwo.setInTournamentEvent(true);
					pairOne.removeSkills();
					pairTwo.removeSkills();
					fights.put(this.arena.id, pairOne.getLeader().getName() + " vs " + pairTwo.getLeader().getName());
					break;
				}
			}
		}
	}*/
		private void portPairsToArena(InstanceHolder instance)
		{
			for (Arena arena : arenas)
			{
			//	if (arena.isFree)
			//	{
					this.arena = arena;
				//	arena.setFree(false);
					pairOne.removePet();
					pairTwo.removePet();
					pairOne.setArenaInstance(instance);
					pairTwo.setArenaInstance(instance);
					pairOne.teleportTo(arena.x - ArenaConfig.RANGE_TOURNAMENT_EVENT_LOC, arena.y, arena.z);
					pairTwo.teleportTo(arena.x + ArenaConfig.RANGE_TOURNAMENT_EVENT_LOC, arena.y, arena.z);
					pairOne.setImobilised(true);
					pairTwo.setImobilised(true);
					pairOne.setInTournamentEvent(true);
					pairTwo.setInTournamentEvent(true);
					pairOne.removeSkills();
					pairTwo.removeSkills();
					fights.put(this.arena.id, pairOne.getLeader().getName() + " vs " + pairTwo.getLeader().getName());
					break;
			//	}
			}
		}
	}	
		
		
		

	private class Arena
	{
		protected int x, y, z;
		@SuppressWarnings("unused")
		protected boolean isFree = true;
		int id;

		public Arena(int id, int x, int y, int z)
		{
			this.id = id;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@SuppressWarnings("unused")
		public void setFree(boolean val)
		{
			isFree = val;
		}
	}

	protected class countdown implements Runnable
	{
		private final Player _player;
		private int _time;

		public countdown(Player player, int time)
		{
			_time = time;
			_player = player;
		}

		@Override
		public void run()
		{
			if (_player.isOnline())
			{

				switch (_time)
				{
				case 300:
				case 240:
				case 180:
				case 120:
				case 57:
					if (_player.isOnline())
					{
						_player.sendPacket(new ExShowScreenMessage("The battle starts in 60 second(s)..", 4000));
						_player.sendMessage("60 second(s) to start the battle.");
					}
					break;
				case 45:
					if (_player.isOnline())
					{
						_player.sendPacket(new ExShowScreenMessage("" + _time + " ..", 3000));
						_player.sendMessage(_time + " second(s) to start the battle!");
					}
					break;
				case 27:
					if (_player.isOnline())
					{
						_player.sendPacket(new ExShowScreenMessage("The battle starts in 30 second(s)..", 4000));
						_player.sendMessage("30 second(s) to start the battle.");
					}
					break;
				case 20:
					if (_player.isOnline())
					{
						_player.sendPacket(new ExShowScreenMessage("" + _time + " ..", 3000));
						_player.sendMessage(_time + " second(s) to start the battle!");
					}
					break;
				case 15:
					if (_player.isOnline())
					{
						_player.sendPacket(new ExShowScreenMessage("" + _time + " ..", 3000));
						_player.sendMessage(_time + " second(s) to start the battle!");
					}
					break;
				case 10:
					if (_player.isOnline())
						_player.sendMessage(_time + " second(s) to start the battle!");
					break;
				case 5:
					if (_player.isOnline())
						_player.sendMessage(_time + " second(s) to start the battle!");
					break;
				case 4:
					if (_player.isOnline())
						_player.sendMessage(_time + " second(s) to start the battle!");
					break;
				case 3:
					if (_player.isOnline())
						_player.sendMessage(_time + " second(s) to start the battle!");
					break;
				case 2:
					if (_player.isOnline())
						_player.sendMessage(_time + " second(s) to start the battle!");
					break;
				case 1:
					if (_player.isOnline())
						_player.sendMessage(_time + " second(s) to start the battle!");
					break;
				}
				if (_time > 1)
				{
					ThreadPool.schedule(new countdown(_player, _time - 1), 1000);
				}
			}
		}
	}

	private static class SingletonHolder
	{
		protected static final Arena1x1 INSTANCE = new Arena1x1();
	}
}