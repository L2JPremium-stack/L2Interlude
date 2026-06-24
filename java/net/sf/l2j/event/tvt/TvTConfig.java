package net.sf.l2j.event.tvt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2j.commons.config.ExProperties;

public class TvTConfig
{
	protected static final Logger _log = Logger.getLogger(TvTConfig.class.getName());
	
	private static final String TVT_FILE = "./config/events/TeamVsTeam.properties";
	
	public static boolean TVT_EVENT_ENABLED;
	public static String[] TVT_EVENT_INTERVAL;
	public static int TVT_EVENT_PARTICIPATION_TIME;
	public static int TVT_EVENT_RUNNING_TIME;
	public static String TVT_NPC_LOC_NAME;
	public static int TVT_EVENT_PARTICIPATION_NPC_ID;
	public static int[] TVT_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
	public static int[] TVT_EVENT_PARTICIPATION_FEE = new int[2];
	public static int TVT_EVENT_MIN_PLAYERS_IN_TEAMS;
	public static int TVT_EVENT_MAX_PLAYERS_IN_TEAMS;
	public static int TVT_EVENT_RESPAWN_TELEPORT_DELAY;
	public static boolean BLOCK_SKILLS_HEAL_TVT;
	public static int TVT_EVENT_START_LEAVE_TELEPORT_DELAY;
	
	public static boolean TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED;
	public static boolean TVT_EVENT_SCROLL_ALLOWED;
	public static boolean TVT_EVENT_POTIONS_ALLOWED;
	public static boolean TVT_EVENT_SUMMON_BY_ITEM_ALLOWED;
	public static byte TVT_EVENT_MIN_LVL;
	public static byte TVT_EVENT_MAX_LVL;
//	public static int TVT_EVENT_EFFECTS_REMOVAL;
	public static Map<Integer, Integer> TVT_EVENT_FIGHTER_BUFFS;
	public static Map<Integer, Integer> TVT_EVENT_MAGE_BUFFS;
	public static boolean TVT_EVENT_MULTIBOX_PROTECTION_ENABLE;
	public static int TVT_EVENT_NUMBER_BOX_REGISTER;
	public static boolean TVT_REWARD_PLAYER;
//	public static boolean TVT_REWARD_NO_CARRIER_PLAYER;
	public static String TVT_EVENT_ON_KILL;
	public static String DISABLE_ID_CLASSES_STRING;
	public static List<Integer> DISABLE_ID_CLASSES;
//	public static boolean ALLOW_TVT_DLG;
	
	public static boolean TVT_PLAYER_CAN_BE_KILLED_IN_PZ;

	public static boolean ALLOW_TvT_COMMANDS;
	
	
	
	public static ExProperties load(String filename)
	{
		return load(new File(filename));
	}
	public static void init()
	{
	    ExProperties events = load(TVT_FILE);

	    ALLOW_TvT_COMMANDS = events.getProperty("TvTEventCommand", false);
	    TVT_EVENT_ENABLED = events.getProperty("TvTEventEnabled", false);
	    TVT_EVENT_INTERVAL = events.getProperty("TvTEventInterval", "20:00").split(",");
	    TVT_EVENT_PARTICIPATION_TIME = events.getProperty("TvTEventParticipationTime", 3600);
	    TVT_EVENT_RUNNING_TIME = events.getProperty("TvTEventRunningTime", 1800);
	    TVT_NPC_LOC_NAME = events.getProperty("TvTNpcLocName", "Giran Town");
	    TVT_EVENT_PARTICIPATION_NPC_ID = events.getProperty("TvTEventParticipationNpcId", 0);

	    if (TVT_EVENT_PARTICIPATION_NPC_ID == 0)
	    {
	        TVT_EVENT_ENABLED = false;
	        _log.warning("TvTEventEngine: invalid config property -> TvTEventParticipationNpcId");
	        return;
	    }

	    String[] propertySplit = events.getProperty("TvTEventParticipationNpcCoordinates", "0,0,0").split(",");
	    if (propertySplit.length < 3)
	    {
	        TVT_EVENT_ENABLED = false;
	        _log.warning("TvTEventEngine: invalid config property -> TvTEventParticipationNpcCoordinates");
	        return;
	    }

	    TVT_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
	    TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
	    TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
	    TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
	    if (propertySplit.length == 4)
	        TVT_EVENT_PARTICIPATION_NPC_COORDINATES[3] = Integer.parseInt(propertySplit[3]);

	    TVT_EVENT_MIN_PLAYERS_IN_TEAMS = events.getProperty("TvTEventMinPlayersInTeams", 1);
	    TVT_EVENT_MAX_PLAYERS_IN_TEAMS = events.getProperty("TvTEventMaxPlayersInTeams", 20);
	    TVT_EVENT_MIN_LVL = Byte.parseByte(events.getProperty("TvTEventMinPlayerLevel", "1"));
	    TVT_EVENT_MAX_LVL = Byte.parseByte(events.getProperty("TvTEventMaxPlayerLevel", "80"));
	    TVT_EVENT_RESPAWN_TELEPORT_DELAY = events.getProperty("TvTEventRespawnTeleportDelay", 20);

	    BLOCK_SKILLS_HEAL_TVT = events.getProperty("BlockSkillsHealInTvT", false);
	    TVT_EVENT_START_LEAVE_TELEPORT_DELAY = events.getProperty("TvTEventStartLeaveTeleportDelay", 20);

	    TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED = events.getProperty("TvTEventTargetTeamMembersAllowed", true);
	    TVT_EVENT_SCROLL_ALLOWED = events.getProperty("TvTEventScrollsAllowed", false);
	    TVT_EVENT_POTIONS_ALLOWED = events.getProperty("TvTEventPotionsAllowed", false);
	    TVT_EVENT_SUMMON_BY_ITEM_ALLOWED = events.getProperty("TvTEventSummonByItemAllowed", false);
	  

	    TVT_EVENT_MULTIBOX_PROTECTION_ENABLE = events.getProperty("TvTEventMultiBoxEnable", false);
	    TVT_EVENT_NUMBER_BOX_REGISTER = events.getProperty("TvTEventNumberBoxRegister", 1);

	    TVT_REWARD_PLAYER = events.getProperty("TvTRewardOnlyKillers", false);

	    TVT_EVENT_ON_KILL = events.getProperty("TvTEventOnKill", "pmteam");

	    DISABLE_ID_CLASSES_STRING = events.getProperty("TvTDisabledForClasses", "");
	    DISABLE_ID_CLASSES = new ArrayList<>();
	    if (!DISABLE_ID_CLASSES_STRING.isEmpty())
	    {
	        for(String class_id : DISABLE_ID_CLASSES_STRING.split(","))
	            DISABLE_ID_CLASSES.add(Integer.parseInt(class_id.trim()));
	    }

	    TVT_PLAYER_CAN_BE_KILLED_IN_PZ = events.getProperty("EnableTvTPeaceZoneAttack", false);
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