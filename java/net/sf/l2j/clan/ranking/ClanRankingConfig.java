package net.sf.l2j.clan.ranking;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import net.sf.l2j.commons.config.ExProperties;
import net.sf.l2j.commons.lang.StringUtil;

public class ClanRankingConfig 
{
	protected static final Logger _log = Logger.getLogger(ClanRankingConfig.class.getName());

	public static final String CLANS_FILE = "./config/events/ClanRanking.properties";

	public static boolean ENABLE_CLAN_RANKING;
	
	public static boolean ENABLE_ANNOUNCE_POINTS_EARNED_ONDAMAGE;
	public static HashMap<Integer, Integer> CLAN_RANKING_BOSS_POINTS_MAIN_DAMAGE = new HashMap<>();
	
	public static boolean ENABLE_ANNOUNCE_POINTS_EARNED_ONKILL;
	public static HashMap<Integer, Integer> CLAN_RANKING_BOSS_POINTS_MAIN_KILLER = new HashMap<>();
	
	public static HashMap<Integer, Integer> CLAN_RANKING_SIEGE_POINTS = new HashMap<>();

	
	public static int[][] TOP_1ST_CLAN_REWARDS;
	public static int[][] TOP_2ND_CLAN_REWARDS;
	public static int[][] TOP_3RD_CLAN_REWARDS;
	
	public static void load() 
	{
		ExProperties eventClan = load(CLANS_FILE);
		ENABLE_CLAN_RANKING = eventClan.getProperty("ClanRankingEnabled", false);
		
		ENABLE_ANNOUNCE_POINTS_EARNED_ONDAMAGE = eventClan.getProperty("EnabledAnnoucePointsOnDamage", false);
		CLAN_RANKING_BOSS_POINTS_MAIN_DAMAGE = parseList(eventClan.getProperty("BossPointsByMainDamage", "").trim().split(";"));
		
		ENABLE_ANNOUNCE_POINTS_EARNED_ONKILL = eventClan.getProperty("EnabledAnnoucePointsOnKill", false);
		CLAN_RANKING_BOSS_POINTS_MAIN_KILLER = parseList(eventClan.getProperty("BossPointsByMainKiller", "").trim().split(";"));
		
		CLAN_RANKING_SIEGE_POINTS = parseList(eventClan.getProperty("CastlePoints", "").trim().split(";"));
		
		TOP_1ST_CLAN_REWARDS = parseItemsList(eventClan.getProperty("1stTopClanReward", "6651,50"));
		TOP_2ND_CLAN_REWARDS = parseItemsList(eventClan.getProperty("2ndTopClanReward", "6651,50"));
		TOP_3RD_CLAN_REWARDS = parseItemsList(eventClan.getProperty("3rdTopClanReward", "6651,50"));
	}

	public static HashMap<Integer, Integer> parseList(String[] pointsTokens)
	{
		HashMap<Integer, Integer> _list = new HashMap<>();
		for (String parseToken : pointsTokens) 
		{
			String[] SubTokens = parseToken.split(",");
			if (SubTokens.length != 2)
				return null; 
			_list.put(Integer.valueOf(Integer.parseInt(SubTokens[0])), Integer.valueOf(Integer.parseInt(SubTokens[1])));
		} 
		return _list;
	}
	
	/**
	 * itemId1,itemNumber1;itemId2,itemNumber2... to the int[n][2] = [itemId1][itemNumber1],[itemId2][itemNumber2]...
	 * @param line
	 * @return an array consisting of parsed items.
	 */
	private static int[][] parseItemsList(String line)
	{
		final String[] propertySplit = line.split(";");
		if (propertySplit.length == 0)
			return null;
		
		int i = 0;
		String[] valueSplit;
		final int[][] result = new int[propertySplit.length][];
		for (String value : propertySplit)
		{
			valueSplit = value.split(",");
			if (valueSplit.length != 2)
			{
				_log.warning(StringUtil.concat("parseItemsList[Config.load()]: invalid entry -> \"", valueSplit[0], "\", should be itemId,itemNumber"));
				return null;
			}
			
			result[i] = new int[2];
			try
			{
				result[i][0] = Integer.parseInt(valueSplit[0]);
			}
			catch (NumberFormatException e)
			{
				_log.warning(StringUtil.concat("parseItemsList[Config.load()]: invalid itemId -> \"", valueSplit[0], "\""));
				return null;
			}
			
			try
			{
				result[i][1] = Integer.parseInt(valueSplit[1]);
			}
			catch (NumberFormatException e)
			{
				_log.warning(StringUtil.concat("parseItemsList[Config.load()]: invalid item number -> \"", valueSplit[1], "\""));
				return null;
			}
			i++;
		}
		return result;
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