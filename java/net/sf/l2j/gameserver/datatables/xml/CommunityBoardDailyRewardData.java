package net.sf.l2j.gameserver.datatables.xml;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.gameserver.model.holder.DailyRewardHolder;
import net.sf.l2j.gameserver.model.holder.DailyRewardSettings;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;

public class CommunityBoardDailyRewardData implements IXmlReader
{
	private final Map<Integer, DailyRewardHolder> _rewards = new HashMap<>();
	
	private DailyRewardSettings _settings;
	
	public CommunityBoardDailyRewardData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_rewards.clear();
		
		parseFile("./data/xml/custom/CommunityBoardDailyRewards.xml");
		
		LOGGER.info("Loaded {" + _rewards.size() + "} Daily Reward entries.");
	}
	
	public void reload()
	{
		_rewards.clear();
		load();
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "dailyRewards", rootNode -> {
			/*
			 * SETTINGS
			 */
			forEach(rootNode, "settings", settingsNode -> {
				_settings = new DailyRewardSettings(parseAttributes(settingsNode));
			});
			
			/*
			 * DAYS
			 */
			forEach(rootNode, "day", dayNode -> {
				final StatsSet set = new StatsSet();
				
				/*
				 * DAY ATTRIBUTES
				 */
				set.set("value", parseInteger(dayNode.getAttributes(), "value"));
				
				/*
				 * REWARD
				 */
				forEach(dayNode, "reward", rewardNode -> {
					final StatsSet rewardSet = parseAttributes(rewardNode);
					
					rewardSet.forEach((key, value) -> {
						set.set(key, value);
					});
				});
				
				/*
				 * MISSION
				 */
				forEach(dayNode, "mission", missionNode -> {
					final StatsSet missionSet = parseAttributes(missionNode);
					
					missionSet.forEach((key, value) -> {
						set.set(key, value);
					});
				});
				
				final DailyRewardHolder holder = new DailyRewardHolder(set);
				
				_rewards.put(holder.getDay(), holder);
			});
		});
	}
	
	public DailyRewardHolder getReward(int day)
	{
		return _rewards.get(day);
	}
	
	public DailyRewardSettings getSettings()
	{
		return _settings;
	}
	
	public Map<Integer, DailyRewardHolder> getRewards()
	{
		return _rewards;
	}
	
	public static CommunityBoardDailyRewardData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final CommunityBoardDailyRewardData INSTANCE = new CommunityBoardDailyRewardData();
	}
}