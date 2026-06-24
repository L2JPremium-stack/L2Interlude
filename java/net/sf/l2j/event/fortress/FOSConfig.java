package net.sf.l2j.event.fortress;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.commons.config.ExProperties;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.event.partyfarm.RewardHolder;

public class FOSConfig
{
	protected static final Logger _log = Logger.getLogger(FOSConfig.class.getName());
	
	private static final String FOS_FILE = "./config/events/TvTFortress.properties";
	
	public static boolean FOS_EVENT_ENABLED;
	public static String[] FOS_EVENT_INTERVAL;
	public static int FOS_EVENT_PARTICIPATION_TIME;
	public static int FOS_EVENT_RUNNING_TIME;
	public static String FOS_NPC_LOC_NAME;
	
	public static int FOS_EVENT_PARTICIPATION_NPC_ID;
	public static int FOS_EVENT_ARTIFACT_NPC_ID;
	public static int FOS_EVENT_SUMMON_SKILL_ID;
//	public static int FOS_EVENT_TEAM_1_FLAG;
//	public static int FOS_EVENT_TEAM_2_FLAG;
//	public static int FOS_EVENT_CAPTURE_SKILL;
	public static int[] FOS_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
	public static int[] FOS_EVENT_PARTICIPATION_FEE = new int[2];
	public static int FOS_EVENT_MIN_PLAYERS_IN_TEAMS;
	public static int FOS_EVENT_MAX_PLAYERS_IN_TEAMS;
	public static int FOS_EVENT_RESPAWN_TELEPORT_DELAY;
	public static int FOS_EVENT_START_LEAVE_TELEPORT_DELAY;
	
	public static String FOS_EVENT_TEAM_1_NAME;
	public static int[] FOS_EVENT_TEAM_1_COORDINATES = new int[3];
	public static String FOS_EVENT_TEAM_2_NAME;
	public static int[] FOS_EVENT_TEAM_2_COORDINATES = new int[3];
	public static int[] FOS_EVENT_FLAG_COORDINATES = new int[4];

	public static List<RewardHolder> FOS_EVENT_REWARDS_WIN = new ArrayList<>();
	public static List<RewardHolder> FOS_EVENT_REWARDS_LOS = new ArrayList<>();
	public static boolean BLOCK_SKILLS_HEAL_FOS;
	public static boolean FOS_EVENT_TARGET_TEAM_MEMBERS_ALLOWED;
	public static boolean FOS_EVENT_SCROLL_ALLOWED;
	public static boolean FOS_EVENT_POTIONS_ALLOWED;
	public static boolean FOS_EVENT_SUMMON_BY_ITEM_ALLOWED;
	public static List<Integer> FOS_DOORS_ATTACKABLE;
	public static boolean FOS_REWARD_TEAM_TIE;
//	public static boolean FOS_REWARD_NO_CARRIER_PLAYER;
	public static byte FOS_EVENT_MIN_LVL;
	public static byte FOS_EVENT_MAX_LVL;
	public static int FOS_EVENT_EFFECTS_REMOVAL;
	public static Map<Integer, Integer> FOS_EVENT_FIGHTER_BUFFS;
	public static Map<Integer, Integer> FOS_EVENT_MAGE_BUFFS;
	public static boolean FOS_EVENT_MULTIBOX_PROTECTION_ENABLE;
	public static int FOS_EVENT_NUMBER_BOX_REGISTER;
	public static boolean ALLOW_TVTFOS_COMMANDS;
	public static boolean ENABLE_AUTO_SKILL_FOS_TVT;
	
	public static boolean FOS_PLAYER_CAN_BE_KILLED_IN_PZ;
//	public static boolean ENABLE_FOS_INSTANCE;
//	public static int FOS_INSTANCE_ID;
	
	public static void init()
	{
		ExProperties events = load(FOS_FILE);
		BLOCK_SKILLS_HEAL_FOS =  events.getProperty("BlockSkillsHealInFOS", false);
		FOS_PLAYER_CAN_BE_KILLED_IN_PZ = events.getProperty("EnableFOSPeaceZoneAttack", false);
//		ENABLE_FOS_INSTANCE = events.getProperty("EnableFOSInstance", false);
//		FOS_INSTANCE_ID = events.getProperty("FOSInstanceId", 1);
		ENABLE_AUTO_SKILL_FOS_TVT = events.getProperty("EnableAutoClickSummonCrest", false); 
		//Novo
		ALLOW_TVTFOS_COMMANDS = events.getProperty("EnableCommandTVTFortress", false);
		FOS_EVENT_ENABLED = events.getProperty("FOSEventEnabled", false);
		FOS_EVENT_INTERVAL = events.getProperty("FOSEventInterval", "20:00").split(",");
		FOS_EVENT_PARTICIPATION_TIME = events.getProperty("FOSEventParticipationTime", 3600);
		FOS_EVENT_RUNNING_TIME = events.getProperty("FOSEventRunningTime", 1800);
		FOS_NPC_LOC_NAME = events.getProperty("FOSNpcLocName", "Giran Town");
		
		FOS_EVENT_PARTICIPATION_NPC_ID = events.getProperty("FOSEventParticipationNpcId", 0);
		FOS_EVENT_ARTIFACT_NPC_ID = events.getProperty("FOSEventArtifactNpcId", 0);
		FOS_EVENT_SUMMON_SKILL_ID = events.getProperty("FOSEventSummonSkillId", 0);
	//	FOS_EVENT_TEAM_1_FLAG = events.getProperty("FOSEventFirstTeamFlag", 0);
	//	FOS_EVENT_TEAM_2_FLAG = events.getProperty("FOSEventSecondTeamFlag", 0);
	//	FOS_EVENT_CAPTURE_SKILL = events.getProperty("FOSEventCaptureSkillId", 0);

		if (FOS_EVENT_PARTICIPATION_NPC_ID == 0)
		{
			FOS_EVENT_ENABLED = false;
				_log.warning("FOSEventEngine[Config.load()]: invalid config property -> FOSEventParticipationNpcId");
		}
		else
		{
			String[] ctfNpcCoords = events.getProperty("FOSEventParticipationNpcCoordinates", "0,0,0").split(",");
			if (ctfNpcCoords.length < 3)
			{
				FOS_EVENT_ENABLED = false;
				_log.warning("FOSEventEngine[Config.load()]: invalid config property -> FOSEventParticipationNpcCoordinates");
			}
			else
			{
				FOS_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
				FOS_EVENT_PARTICIPATION_NPC_COORDINATES[0] = Integer.parseInt(ctfNpcCoords[0]);
				FOS_EVENT_PARTICIPATION_NPC_COORDINATES[1] = Integer.parseInt(ctfNpcCoords[1]);
				FOS_EVENT_PARTICIPATION_NPC_COORDINATES[2] = Integer.parseInt(ctfNpcCoords[2]);
				if (ctfNpcCoords.length == 4)
				{
					FOS_EVENT_PARTICIPATION_NPC_COORDINATES[3] = Integer.parseInt(ctfNpcCoords[3]);
				}
				
				//FOS_EVENT_REWARDS = new ArrayList<>();
				FOS_DOORS_ATTACKABLE = new ArrayList<>();

				FOS_EVENT_TEAM_1_COORDINATES = new int[3];
				FOS_EVENT_TEAM_2_COORDINATES = new int[3];

				FOS_EVENT_MIN_PLAYERS_IN_TEAMS = events.getProperty("FOSEventMinPlayersInTeams", 1);
				FOS_EVENT_MAX_PLAYERS_IN_TEAMS = events.getProperty("FOSEventMaxPlayersInTeams", 20);
				FOS_EVENT_MIN_LVL = Byte.parseByte(events.getProperty("FOSEventMinPlayerLevel", "1"));
				FOS_EVENT_MAX_LVL = Byte.parseByte(events.getProperty("FOSEventMaxPlayerLevel", "80"));
				FOS_EVENT_RESPAWN_TELEPORT_DELAY = events.getProperty("FOSEventRespawnTeleportDelay", 20);
				FOS_EVENT_START_LEAVE_TELEPORT_DELAY = events.getProperty("FOSEventStartLeaveTeleportDelay", 20);
				FOS_EVENT_EFFECTS_REMOVAL = events.getProperty("FOSEventEffectsRemoval", 0);
				FOS_EVENT_TEAM_1_NAME = events.getProperty("FOSEventTeam1Name", "Team1");
				FOS_EVENT_TEAM_2_NAME = events.getProperty("FOSEventTeam2Name", "Team2");
				ctfNpcCoords = events.getProperty("FOSEventTeam1Coordinates", "0,0,0").split(",");
				if (ctfNpcCoords.length < 3)
				{
					FOS_EVENT_ENABLED = false;
					_log.warning("FOSEventEngine[Config.load()]: invalid config property -> FOSEventTeam1Coordinates");
				}
				else
				{
					FOS_EVENT_TEAM_1_COORDINATES[0] = Integer.parseInt(ctfNpcCoords[0]);
					FOS_EVENT_TEAM_1_COORDINATES[1] = Integer.parseInt(ctfNpcCoords[1]);
					FOS_EVENT_TEAM_1_COORDINATES[2] = Integer.parseInt(ctfNpcCoords[2]);
					ctfNpcCoords = events.getProperty("FOSEventTeam2Coordinates", "0,0,0").split(",");
					if (ctfNpcCoords.length < 3)
					{
						FOS_EVENT_ENABLED = false;
						_log.warning("FOSEventEngine[Config.load()]: invalid config property -> FOSEventTeam2Coordinates");
					}
					else
					{
						FOS_EVENT_TEAM_2_COORDINATES[0] = Integer.parseInt(ctfNpcCoords[0]);
						FOS_EVENT_TEAM_2_COORDINATES[1] = Integer.parseInt(ctfNpcCoords[1]);
						FOS_EVENT_TEAM_2_COORDINATES[2] = Integer.parseInt(ctfNpcCoords[2]);
						
						//QUARTEL 1
						if (FOS_EVENT_ARTIFACT_NPC_ID == 0)
						{
							FOS_EVENT_ENABLED = false;
							_log.warning("FOSEventEngine[Config.load()]: invalid config property -> FOSEventSecondTeamHeadquartersId");
						}
						else
						{
							ctfNpcCoords = events.getProperty("FOSEventArtifactCoordinates", "0,0,0").split(",");
							if (ctfNpcCoords.length < 3)
							{
								FOS_EVENT_ENABLED = false;
								_log.warning("FOSEventEngine[Config.load()]: invalid config property -> FOSEventTeam2FlagCoordinates");
							}
							else
							{
								FOS_EVENT_FLAG_COORDINATES = new int[4];
								FOS_EVENT_FLAG_COORDINATES[0] = Integer.parseInt(ctfNpcCoords[0]);
								FOS_EVENT_FLAG_COORDINATES[1] = Integer.parseInt(ctfNpcCoords[1]);
								FOS_EVENT_FLAG_COORDINATES[2] = Integer.parseInt(ctfNpcCoords[2]);
								if (ctfNpcCoords.length == 4)
								{
									FOS_EVENT_FLAG_COORDINATES[3] = Integer.parseInt(ctfNpcCoords[3]);
								}
							}

							ctfNpcCoords = events.getProperty("FOSEventParticipationFee", "0,0").split(",");
							try
							{
								FOS_EVENT_PARTICIPATION_FEE[0] = Integer.parseInt(ctfNpcCoords[0]);
								FOS_EVENT_PARTICIPATION_FEE[1] = Integer.parseInt(ctfNpcCoords[1]);
							}
							catch (NumberFormatException nfe)
							{
								if (ctfNpcCoords.length > 0)
								{
									_log.warning("FOSEventEngine[Config.load()]: invalid config property -> FOSEventParticipationFee");
								}
							}
		
							/*
							ctfNpcCoords = events.getProperty("FOSEventReward", "57,100000").split(";");
							for (String reward : ctfNpcCoords)
							{
								String[] rewardSplit = reward.split(",");
								if (rewardSplit.length != 2)
								{
									_log.warning(StringUtil.concat("FOSEventEngine[Config.load()]: invalid config property -> FOSEventReward \"", reward, "\""));
								}
								else
								{
									try
									{
										FOS_EVENT_REWARDS.add(new int[]
												{
												Integer.parseInt(rewardSplit[0]),
												Integer.parseInt(rewardSplit[1])
												});
									}
									catch (NumberFormatException nfe)
									{
										if (!reward.isEmpty())
										{
											_log.warning(StringUtil.concat("FOSEventEngine[Config.load()]: invalid config property -> FOSEventReward \"", reward, "\""));
										}
									}
								}
							}
							*/
						}
					
					    FOS_EVENT_REWARDS_WIN = Config.parseReward(events, "FOSEventRewardWinners");
					    FOS_EVENT_REWARDS_LOS = Config.parseReward(events, "FOSEventRewardLosers");
					
						FOS_EVENT_MULTIBOX_PROTECTION_ENABLE = events.getProperty("FOSEventMultiBoxEnable", false);
						FOS_EVENT_NUMBER_BOX_REGISTER = events.getProperty("FOSEventNumberBoxRegister", 1);
						FOS_EVENT_TARGET_TEAM_MEMBERS_ALLOWED = events.getProperty("FOSEventTargetTeamMembersAllowed", true);
						FOS_EVENT_SCROLL_ALLOWED = events.getProperty("FOSEventScrollsAllowed", false);
						FOS_EVENT_POTIONS_ALLOWED = events.getProperty("FOSEventPotionsAllowed", false);
						FOS_EVENT_SUMMON_BY_ITEM_ALLOWED = events.getProperty("FOSEventSummonByItemAllowed", false);
						FOS_REWARD_TEAM_TIE = events.getProperty("FOSRewardTeamTie", false);
					//	FOS_REWARD_NO_CARRIER_PLAYER = events.getProperty("FOSRewardNoCarrierPlayers", false);
						ctfNpcCoords = events.getProperty("FOSDoorsAttackable", "").split(";");
						for (String door : ctfNpcCoords)
						{
							try
							{
								FOS_DOORS_ATTACKABLE.add(Integer.parseInt(door));
							}
							catch (NumberFormatException nfe)
							{
								if (!door.isEmpty())
								{
									_log.warning(StringUtil.concat("FOSEventEngine[Config.load()]: invalid config property -> FOSDoorsAttackable \"", door, "\""));
								}
							}
						}

						ctfNpcCoords = events.getProperty("FOSEventFighterBuffs", "").split(";");
						if (!ctfNpcCoords[0].isEmpty())
						{
							FOS_EVENT_FIGHTER_BUFFS = new HashMap<>(ctfNpcCoords.length);
							for (String skill : ctfNpcCoords)
							{
								String[] skillSplit = skill.split(",");
								if (skillSplit.length != 2)
								{
									_log.warning(StringUtil.concat("FOSEventEngine[Config.load()]: invalid config property -> FOSEventFighterBuffs \"", skill, "\""));
								}
								else
								{
									try
									{
										FOS_EVENT_FIGHTER_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
									}
									catch (NumberFormatException nfe)
									{
										if (!skill.isEmpty())
										{
											_log.warning(StringUtil.concat("FOSEventEngine[Config.load()]: invalid config property -> FOSEventFighterBuffs \"", skill, "\""));
										}
									}
								}
							}
						}
							
						ctfNpcCoords = events.getProperty("FOSEventMageBuffs", "").split(";");
						if (!ctfNpcCoords[0].isEmpty())
						{
							FOS_EVENT_MAGE_BUFFS = new HashMap<>(ctfNpcCoords.length);
							for (String skill : ctfNpcCoords)
							{
								String[] skillSplit = skill.split(",");
								if (skillSplit.length != 2)
								{
									_log.warning(StringUtil.concat("FOSEventEngine[Config.load()]: invalid config property -> FOSEventMageBuffs \"", skill, "\""));
								}
								else
									{
									try
									{
										FOS_EVENT_MAGE_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
									}
									catch (NumberFormatException nfe)
									{
										if (!skill.isEmpty())
										{
											_log.warning(StringUtil.concat("FOSEventEngine[Config.load()]: invalid config property -> FOSEventMageBuffs \"", skill, "\""));
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static ExProperties load(String filename)
	{
		return load(new File(filename));
	}
	
	public static ExProperties load(File file)
	{
		ExProperties result = new ExProperties();
		
		try
		{
			result.load(file);
		}
		catch (IOException e)
		{
			_log.warning("Error loading config : " + file.getName() + "!");
		}
		
		return result;
	}
}