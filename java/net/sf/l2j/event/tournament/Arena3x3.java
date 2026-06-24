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

public class Arena3x3 implements Runnable
{
	// list of participants
	public static List<Pair> registered;
	// number of Arenas
	int free = ArenaConfig.ARENA_EVENT_COUNT_3X3;
	// Arenas
	Arena[] arenas = new Arena[ArenaConfig.ARENA_EVENT_COUNT_3X3];
	// list of fights going on
	Map<Integer, String> fights = new HashMap<>(ArenaConfig.ARENA_EVENT_COUNT_3X3);

	public Arena3x3()
	{
		registered = new ArrayList<>();
		int[] coord;
		for (int i = 0; i < ArenaConfig.ARENA_EVENT_COUNT_3X3; i++)
		{
			coord = ArenaConfig.ARENA_EVENT_LOCS_3X3[i];
			arenas[i] = new Arena(i, coord[0], coord[1], coord[2]);
		}
	}

	public static Arena3x3 getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	public boolean register(Player player, Player assist, Player assist2)
	{
		for (Pair p : registered)
		{
			if (p.getLeader() == player || p.getAssist() == player)
			{
				player.sendMessage("Tournament: You already registered!");
				return false;
			}
			else if (p.getLeader() == assist || p.getAssist() == assist)
			{
				player.sendMessage("Tournament: " + assist.getName() + " already registered!");
				return false;
			}
			else if (p.getLeader() == assist2 || p.getAssist2() == assist2)
			{
				player.sendMessage("Tournament: " + assist2.getName() + " already registered!");
				return false;
			}
		}
		return registered.add(new Pair(player, assist, assist2));
	}

	public boolean isRegistered(Player player)
	{
		for (Pair p : registered)
		{
			if (p.getLeader() == player || p.getAssist() == player || p.getAssist2() == player)
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
			if (p.getLeader() == player || p.getAssist() == player || p.getAssist2() == player)
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
	//		if (registered.size() < 2 || free == 0)
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
		private Player leader, assist, assist2;

		public Pair(Player leader, Player assist, Player assist2)
		{
			this.leader = leader;
			this.assist = assist;
			this.assist2 = assist2;
		}

		public Player getAssist()
		{
			return assist;
		}

		public Player getAssist2()
		{
			return assist2;
		}

		public Player getLeader()
		{
			return leader;
		}

		public boolean check()
		{
			if ((leader == null || !leader.isOnline()))
			{
				if (assist != null || assist.isOnline())
					assist.sendMessage("Tournament: You participation in Event was Canceled.");

				if (assist2 != null || assist2.isOnline())
					assist2.sendMessage("Tournament: You participation in Event was Canceled.");

				return false;
			}
			
			else if (((assist == null || !assist.isOnline()) || (assist2 == null || !assist2.isOnline()) && (leader != null || leader.isOnline())))
			{
				leader.sendMessage("Tournament: You participation in Event was Canceled.");

				if (assist != null || assist.isOnline())
					assist.sendMessage("Tournament: You participation in Event was Canceled.");

				if (assist2 != null || assist2.isOnline())
					assist2.sendMessage("Tournament: You participation in Event was Canceled.");

				return false;
			}
			return true;
		}

		public boolean isDead()
		{
			if (ArenaConfig.ARENA_PROTECT)
			{
				if (leader != null && leader.isOnline() && leader.isArenaAttack() && !leader.isDead() && !leader.isInsideZone(ZoneId.ARENA_EVENT))
					leader.logout(true);
				if (assist != null && assist.isOnline() && assist.isArenaAttack() && !assist.isDead() && !assist.isInsideZone(ZoneId.ARENA_EVENT))
					assist.logout(true);
				if (assist2 != null && assist2.isOnline() && assist2.isArenaAttack() && !assist2.isDead() && !assist2.isInsideZone(ZoneId.ARENA_EVENT))
					assist2.logout(true);
			}

			if ((leader == null || leader.isDead() || !leader.isOnline() || !leader.isInsideZone(ZoneId.ARENA_EVENT) || !leader.isArenaAttack()) && (assist == null || assist.isDead() || !assist.isOnline() || !assist.isInsideZone(ZoneId.ARENA_EVENT) || !assist.isArenaAttack()) && (assist2 == null || assist2.isDead() || !assist2.isOnline() || !assist2.isInsideZone(ZoneId.ARENA_EVENT)))
				return false;

			return !(leader.isDead() && assist.isDead() && assist2.isDead());
		}

		public boolean isAlive()
		{
			if ((leader == null || leader.isDead() || !leader.isOnline() || !leader.isInsideZone(ZoneId.ARENA_EVENT) || !leader.isArenaAttack()) && (assist == null || assist.isDead() || !assist.isOnline() || !assist.isInsideZone(ZoneId.ARENA_EVENT) || !assist.isArenaAttack()) && (assist2 == null || assist2.isDead() || !assist2.isOnline() || !assist2.isInsideZone(ZoneId.ARENA_EVENT)))
				return false;

			return !(leader.isDead() && assist.isDead() && assist2.isDead());
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
			if (assist != null && assist.isOnline())
			{
			//	assist.getAppearance().setInvisible();
				assist.setCurrentCp(assist.getMaxCp());
				assist.setCurrentHp(assist.getMaxHp());
				assist.setCurrentMp(assist.getMaxMp());
				assist.setTeamTour1(false);
				assist.setTeamTour2(false);
				//arena observer
				/*
				if (assist.isInObserverMode())
				{
					assist.setLastCords(x, y + 200, z);
					assist.leaveOlympiadObserverMode();
				}
				 */
				if (!assist.isInJail())
					assist.teleToLocation(x, y + 200, z, 0);

				assist.broadcastUserInfo();
			}
			if (assist2 != null && assist2.isOnline())
			{
			//	assist2.getAppearance().setInvisible();
				assist2.setCurrentCp(assist2.getMaxCp());
				assist2.setCurrentHp(assist2.getMaxHp());
				assist2.setCurrentMp(assist2.getMaxMp());
				assist2.setTeamTour1(false);
				assist2.setTeamTour2(false);

				/*
				if (assist2.isInObserverMode())
				{
					assist2.setLastCords(x, y + 150, z);
					assist2.leaveOlympiadObserverMode();
				}
				 */
				if (!assist2.isInJail())
					assist2.teleToLocation(x, y + 150, z, 0);

				assist2.broadcastUserInfo();
			}
		}
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
			if (assist != null && assist.isOnline())
			{
			//	assist.setTitle(title);
			//	assist.getAppearance().setTitleColor(Integer.decode("0x" + color));
				assist.setTeamTour1(true);
				assist.broadcastUserInfo();
				assist.broadcastTitleInfo();
			}
			if (assist2 != null && assist2.isOnline())
			{
			//	assist2.setTitle(title);
			//	assist2.getAppearance().setTitleColor(Integer.decode("0x" + color));
				assist2.setTeamTour1(true);
				assist2.broadcastUserInfo();
				assist2.broadcastTitleInfo();
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
			if (assist != null && assist.isOnline())
			{
			//	assist.setTitle(title);
			//	assist.getAppearance().setTitleColor(Integer.decode("0x" + color));
				assist.setTeamTour2(true);
				assist.broadcastUserInfo();
				assist.broadcastTitleInfo();
			}
			if (assist2 != null && assist2.isOnline())
			{
			//	assist2.setTitle(title);
			//	assist2.getAppearance().setTitleColor(Integer.decode("0x" + color));
				assist2.setTeamTour2(true);
				assist2.broadcastUserInfo();
				assist2.broadcastTitleInfo();
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
//
//			if (assist != null && assist.isOnline())
//			{
//				assist.setTitle(title);
//				assist.getAppearance().setTitleColor(Integer.decode("0x" + color));
//				assist.broadcastUserInfo();
//				assist.broadcastTitleInfo();
//			}
//			if (assist2 != null && assist2.isOnline())
//			{
//				assist2.setTitle(title);
//				assist2.getAppearance().setTitleColor(Integer.decode("0x" + color));
//				assist2.broadcastUserInfo();
//				assist2.broadcastTitleInfo();
//			}
//		}

		public void saveTitle()
		{
			if (leader != null && leader.isOnline())
			{
				leader._originalTitleColorTournament = leader.getAppearance().getTitleColor();
				leader._originalTitleTournament = leader.getTitle();
			}

			if (assist != null && assist.isOnline())
			{
				assist._originalTitleColorTournament = assist.getAppearance().getTitleColor();
				assist._originalTitleTournament = assist.getTitle();
			}

			if (assist2 != null && assist2.isOnline())
			{
				assist2._originalTitleColorTournament = assist2.getAppearance().getTitleColor();
				assist2._originalTitleTournament = assist2.getTitle();
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

			if (assist != null && assist.isOnline())
			{
				assist.setTitle(assist._originalTitleTournament);
				assist.getAppearance().setTitleColor(assist._originalTitleColorTournament);
				assist.broadcastUserInfo();
				assist.broadcastTitleInfo();
			}

			if (assist2 != null && assist2.isOnline())
			{
				assist2.setTitle(assist2._originalTitleTournament);
				assist2.getAppearance().setTitleColor(assist2._originalTitleColorTournament);
				assist2.broadcastUserInfo();
				assist2.broadcastTitleInfo();
			}
		}

		public void setArenaInstance(InstanceHolder instance) 
		{
			
			if (leader != null && leader.isOnline())
				leader.setInstance(instance, true); //3x3 Tournament Instance
			
			if (assist != null && assist.isOnline())
				assist.setInstance(instance, true); //3x3 Tournament Instance
			
			if (assist2 != null && assist2.isOnline())
				assist2.setInstance(instance, true); //3x3 Tournament Instance
		}

		public void setRealInstance() 
		{
			InstanceHolder instance = InstanceManager.getInstance().getInstance(0);
			if (leader != null && leader.isOnline())
				leader.setInstance(instance, true);
			
			if (assist != null && assist.isOnline())
				assist.setInstance(instance, true);
			
			if (assist2 != null && assist2.isOnline())
				assist2.setInstance(instance, true);
		}
		
		public void rewards()
		{
			/*if (Config.ACTIVE_MISSION_TOURNAMENT)
			{							
				if (!leader.checkMissions(leader.getObjectId()))
					leader.updateMissions();

				if (!(leader.isTournamentCompleted() || leader.getTournamentCont() >= Config.MISSION_TOURNAMENT_COUNT))
					leader.setTournamentCont(leader.getTournamentCont() + 1);
				
				if (!assist.checkMissions(assist.getObjectId()))
					assist.updateMissions();

				if (!(assist.isTournamentCompleted() || assist.getTournamentCont() >= Config.MISSION_TOURNAMENT_COUNT))
					assist.setTournamentCont(assist.getTournamentCont() + 1);
				
				if (!assist2.checkMissions(assist2.getObjectId()))
					assist2.updateMissions();

				if (!(assist2.isTournamentCompleted() || assist2.getTournamentCont() >= Config.MISSION_TOURNAMENT_COUNT))
					assist2.setTournamentCont(assist2.getTournamentCont() + 1);
			}*/
			
			if (leader != null && leader.isOnline())
			{
				if (leader.isVip())
					leader.addItem("Arena_Event", ArenaConfig.ARENA_REWARD_ID, (int) (ArenaConfig.ARENA_WIN_REWARD_COUNT_3X3 * Config.VIP_DROP_RATE), leader, true);
				else
					leader.addItem("Arena_Event", ArenaConfig.ARENA_REWARD_ID, ArenaConfig.ARENA_WIN_REWARD_COUNT_3X3, leader, true);
				
				if (Config.ACTIVE_MISSION_3X3)
				{
					if (!this.leader.check_obj_mission(this.leader.getObjectId()))
						this.leader.updateMission(); 
					if (!this.leader.is2x2Completed() && this.leader.getTournament2x2Cont() < Config.MISSION_3X3_CONT)
						this.leader.setTournament2x2Cont(this.leader.getTournament2x2Cont() + 1); 
				} 
				
			}

			if (assist != null && assist.isOnline())
			{
				if (assist.isVip())
					assist.addItem("Arena_Event", ArenaConfig.ARENA_REWARD_ID, (int) (ArenaConfig.ARENA_WIN_REWARD_COUNT_3X3 * Config.VIP_DROP_RATE), assist, true);
				else
					assist.addItem("Arena_Event", ArenaConfig.ARENA_REWARD_ID, ArenaConfig.ARENA_WIN_REWARD_COUNT_3X3, assist, true);
				
				if (Config.ACTIVE_MISSION_3X3)
				{
		            if (!this.assist.check_obj_mission(this.assist.getObjectId()))
		              this.assist.updateMission(); 
		            if (!this.assist.is2x2Completed() && this.assist.getTournament2x2Cont() < Config.MISSION_3X3_CONT)
		              this.assist.setTournament2x2Cont(this.assist.getTournament2x2Cont() + 1); 
		          } 
			}

			if (assist2 != null && assist2.isOnline())
			{
				if (assist2.isVip())
					assist2.addItem("Arena_Event", ArenaConfig.ARENA_REWARD_ID, (int) (ArenaConfig.ARENA_WIN_REWARD_COUNT_3X3 * Config.VIP_DROP_RATE), assist2, true);
				else
					assist2.addItem("Arena_Event", ArenaConfig.ARENA_REWARD_ID, ArenaConfig.ARENA_WIN_REWARD_COUNT_3X3, assist2, true);
				
				if (Config.ACTIVE_MISSION_3X3)
				{
					if (!this.assist2.check_obj_mission(this.assist2.getObjectId()))
						this.assist2.updateMission(); 
					if (!this.assist2.is2x2Completed() && this.assist2.getTournament2x2Cont() < Config.MISSION_3X3_CONT)
						this.assist2.setTournament2x2Cont(this.assist2.getTournament2x2Cont() + 1); 
				} 
			}

			sendPacket("Congratulations, your team won the event!", 5);
		}

		public void rewardsLost()
		{
			if (leader != null && leader.isOnline())
			{
				if (leader.isVip())
					leader.addItem("Arena_Event", ArenaConfig.ARENA_REWARD_ID, (int) (ArenaConfig.ARENA_LOST_REWARD_COUNT_3X3 * Config.VIP_DROP_RATE), leader, true);
				else
					leader.addItem("Arena_Event", ArenaConfig.ARENA_REWARD_ID, ArenaConfig.ARENA_LOST_REWARD_COUNT_3X3, leader, true);
			}

			if (assist != null && assist.isOnline())
			{
				if (assist.isVip())
					assist.addItem("Arena_Event", ArenaConfig.ARENA_REWARD_ID, (int) (ArenaConfig.ARENA_LOST_REWARD_COUNT_3X3 * Config.VIP_DROP_RATE), assist, true);
				else
					assist.addItem("Arena_Event", ArenaConfig.ARENA_REWARD_ID, ArenaConfig.ARENA_LOST_REWARD_COUNT_3X3, assist, true);
			}
			if (assist2 != null && assist2.isOnline())
			{
				if (assist2.isVip())
					assist2.addItem("Arena_Event", ArenaConfig.ARENA_REWARD_ID, (int) (ArenaConfig.ARENA_LOST_REWARD_COUNT_3X3 * Config.VIP_DROP_RATE), assist2, true);
				else
					assist2.addItem("Arena_Event", ArenaConfig.ARENA_REWARD_ID, ArenaConfig.ARENA_LOST_REWARD_COUNT_3X3, assist2, true);
			}

			sendPacket("your team lost the event! =(", 5);
		}

		public void setInTournamentEvent(boolean val)
		{
			if (leader != null && leader.isOnline())
				leader.setInArenaEvent(val);

			if (assist != null && assist.isOnline())
				assist.setInArenaEvent(val);

			if (assist2 != null && assist2.isOnline())
				assist2.setInArenaEvent(val);
		}

		public void removeMessage()
		{
			if (leader != null && leader.isOnline())
			{
				leader.sendMessage("Tournament: Your participation has been removed.");
				leader.setArenaProtection(false);
				leader.setArena3x3(false);
			}

			if (assist != null && assist.isOnline())
			{
				assist.sendMessage("Tournament: Your participation has been removed.");
				assist.setArenaProtection(false);
				assist.setArena3x3(false);
			}

			if (assist2 != null && assist2.isOnline())
			{
				assist2.sendMessage("Tournament: Your participation has been removed.");
				assist2.setArenaProtection(false);
				assist2.setArena3x3(false);
			}
		}

		public void setArenaProtection(boolean val)
		{
			if (leader != null && leader.isOnline())
			{
				leader.setArenaProtection(val);
				leader.setArena3x3(val);
			}

			if (assist != null && assist.isOnline())
			{
				assist.setArenaProtection(val);
				assist.setArena3x3(val);
			}
			if (assist2 != null && assist2.isOnline())
			{
				assist2.setArenaProtection(val);
				assist2.setArena3x3(val);
			}
		}

		public void revive()
		{
			if (leader != null && leader.isOnline() && leader.isDead())
				leader.doRevive();

			if (assist != null && assist.isOnline() && assist.isDead())
				assist.doRevive();

			if (assist2 != null && assist2.isOnline() && assist2.isDead())
				assist2.doRevive();
		}

		public void setImobilised(boolean val)
		{
			if (leader != null && leader.isOnline())
			{
				leader.setIsInvul(val);
				leader.setStopArena(val);
			}
			if (assist != null && assist.isOnline())
			{
				assist.setIsInvul(val);
				assist.setStopArena(val);
			}
			if (assist2 != null && assist2.isOnline())
			{
				assist2.setIsInvul(val);
				assist2.setStopArena(val);
			}
		}

		public void setArenaAttack(boolean val)
		{
			if (leader != null && leader.isOnline())
			{
				leader.setArenaAttack(val);
				leader.broadcastUserInfo();
			}

			if (assist != null && assist.isOnline())
			{
				assist.setArenaAttack(val);
				assist.broadcastUserInfo();
			}

			if (assist2 != null && assist2.isOnline())
			{
				assist2.setArenaAttack(val);
				assist2.broadcastUserInfo();
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

			if (assist != null && assist.isOnline())
			{
				// Remove Summon's buffs
				if (assist.getPet() != null)
				{
					L2Summon summon = assist.getPet();
					if (summon != null)
						summon.unSummon(summon.getOwner());

					if (summon instanceof L2PetInstance)
						summon.unSummon(assist);
				}

				if (assist.getMountType() == 1 || assist.getMountType() == 2)
					assist.dismount();
			}

			if (assist2 != null && assist2.isOnline())
			{
				// Remove Summon's buffs
				if (assist2.getPet() != null)
				{
					L2Summon summon = assist2.getPet();
					if (summon != null)
						summon.unSummon(summon.getOwner());

					if (summon instanceof L2PetInstance)
						summon.unSummon(assist2);

				}

				if (assist2.getMountType() == 1 || assist2.getMountType() == 2)
					assist2.dismount();
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

			if (!(assist.getClassId() == ClassId.SHILLIEN_ELDER || assist.getClassId() == ClassId.SHILLIEN_SAINT || assist.getClassId() == ClassId.BISHOP || assist.getClassId() == ClassId.CARDINAL || assist.getClassId() == ClassId.ELVEN_ELDER || assist.getClassId() == ClassId.EVAS_SAINT))
			{
				for (L2Effect effect : assist.getAllEffects())
				{
					if (ArenaConfig.ARENA_STOP_SKILL_LIST.contains(effect.getSkill().getId()))
						assist.stopSkillEffects(effect.getSkill().getId());
				}
			}

			if (!(assist2.getClassId() == ClassId.SHILLIEN_ELDER || assist2.getClassId() == ClassId.SHILLIEN_SAINT || assist2.getClassId() == ClassId.BISHOP || assist2.getClassId() == ClassId.CARDINAL || assist2.getClassId() == ClassId.ELVEN_ELDER || assist2.getClassId() == ClassId.EVAS_SAINT))
			{
				for (L2Effect effect : assist2.getAllEffects())
				{
					if (ArenaConfig.ARENA_STOP_SKILL_LIST.contains(effect.getSkill().getId()))
						assist2.stopSkillEffects(effect.getSkill().getId());
				}
			}
		}

		public void sendPacket(String message, int duration)
		{
			if (leader != null && leader.isOnline())
				leader.sendPacket(new ExShowScreenMessage(message, duration * 1000));

			if (assist != null && assist.isOnline())
				assist.sendPacket(new ExShowScreenMessage(message, duration * 1000));

			if (assist2 != null && assist2.isOnline())
				assist2.sendPacket(new ExShowScreenMessage(message, duration * 1000));
		}

		public void inicarContagem(int duration)
		{
			if (leader != null && leader.isOnline())
				ThreadPool.schedule(new countdown(leader, duration), 0);

			if (assist != null && assist.isOnline())
				ThreadPool.schedule(new countdown(assist, duration), 0);

			if (assist2 != null && assist2.isOnline())
				ThreadPool.schedule(new countdown(assist2, duration), 0);
		}

		public void sendPacketinit(String message, int duration)
		{
			if (leader != null && leader.isOnline())
				leader.sendPacket(new ExShowScreenMessage(message, duration * 1000, ExShowScreenMessage.SMPOS.MIDDLE_LEFT, false));

			if (assist != null && assist.isOnline())
				assist.sendPacket(new ExShowScreenMessage(message, duration * 1000, ExShowScreenMessage.SMPOS.MIDDLE_LEFT, false));

			if (assist2 != null && assist2.isOnline())
				assist2.sendPacket(new ExShowScreenMessage(message, duration * 1000, ExShowScreenMessage.SMPOS.MIDDLE_LEFT, false));
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
		//	free--;
			pairOne.saveTitle();
			pairTwo.saveTitle();
			portPairsToArena(instance);
			pairOne.inicarContagem(ArenaConfig.ARENA_WAIT_INTERVAL_3X3);
			pairTwo.inicarContagem(ArenaConfig.ARENA_WAIT_INTERVAL_3X3);
			try
			{
				Thread.sleep(ArenaConfig.ARENA_WAIT_INTERVAL_3X3 * 1000);
			}
			catch (InterruptedException e1)
			{
			}
			pairOne.sendPacketinit("Started. Good Fight!", 3);
			pairTwo.sendPacketinit("Started. Good Fight!", 3);
		//	pairOne.EventTitle(ArenaConfig.MSG_TEAM1, ArenaConfig.TITLE_COLOR_TEAM1);
		//	pairTwo.EventTitle(ArenaConfig.MSG_TEAM2, ArenaConfig.TITLE_COLOR_TEAM2);
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
		//	arena.setFree(true);
		}

		private void rewardWinner()
		{
			if (pairOne.isAlive() && !pairTwo.isAlive())
			{
				Player leader1 = pairOne.getLeader();
				Player leader2 = pairTwo.getLeader();

				if (leader1.getClan() != null && leader2.getClan() != null && ArenaConfig.TOURNAMENT_EVENT_ANNOUNCE)
					Broadcast.gameAnnounceToOnlinePlayers("[ 3X3 ]: [" + leader1.getClan().getName() + " VS " + leader2.getClan().getName() + "] ~> " + leader1.getClan().getName() + " WINS!");

				pairOne.rewards();
				pairTwo.rewardsLost();
			}
			else if (pairTwo.isAlive() && !pairOne.isAlive())
			{
				Player leader1 = pairTwo.getLeader();
				Player leader2 = pairOne.getLeader();

				if (leader1.getClan() != null && leader2.getClan() != null && ArenaConfig.TOURNAMENT_EVENT_ANNOUNCE)
					Broadcast.gameAnnounceToOnlinePlayers("[ 3X3 ]: [" + leader1.getClan().getName() + " VS " + leader2.getClan().getName() + "] ~> " + leader1.getClan().getName() + " WINS!");
				
				pairTwo.rewards();
				pairOne.rewardsLost();
			}
		}

		private boolean check()
		{
			return (pairOne.isDead() && pairTwo.isDead());
		}

		
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
				//}
			}
		}
	}
		
		/*private void portPairsToArena(Instance instance)
		{
			
				int id=1;
				int[] coord = ArenaConfig.ARENA_EVENT_LOCS_3X3[0];
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
					//fights.put(this.arena.id, pairOne.getLeader().getName() + " vs " + pairTwo.getLeader().getName());
					break;
				}
			}
		}
	}*/

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
		protected static final Arena3x3 INSTANCE = new Arena3x3();
	}
}