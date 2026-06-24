package net.sf.l2j.solofarm.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.solofarm.holder.SoloFarmConfig;
import net.sf.l2j.solofarm.holder.SoloFarmReward;

import org.w3c.dom.Document;

public class SoloFarmData implements IXmlReader
{
	private static final String FILE_PATH = "./data/xml/custom/soloFarm.xml";
	
	private SoloFarmConfig _config;
	
	public SoloFarmData()
	{
		load();
	}
	
	public void reload()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_config = null;
		parseFile(FILE_PATH);
		LOGGER.info("SoloFarmData: Loaded.");
 
	}
	@Override
	public void parseDocument(Document doc, Path path)
	{
	    forEach(doc.getDocumentElement(), "soloFarm", eventNode ->
	    {
	        final StatsSet set = new StatsSet();

	        set.set("enabled", parseBoolean(eventNode.getAttributes(), "enabled"));
	        set.set("name", parseString(eventNode.getAttributes(), "name"));

	        final List<SoloFarmReward> rewards = new ArrayList<>();

	        forEach(eventNode, child ->
	        {
	            switch (child.getNodeName())
	            {
	                case "entryNpc":
	                    set.set("entryNpcId", parseInteger(child.getAttributes(), "id"));
	                    break;

	                case "entryLocation":
	                    set.set("entryX", parseInteger(child.getAttributes(), "x"));
	                    set.set("entryY", parseInteger(child.getAttributes(), "y"));
	                    set.set("entryZ", parseInteger(child.getAttributes(), "z"));
	                    break;

	                case "exitLocation":
	                    set.set("exitX", parseInteger(child.getAttributes(), "x"));
	                    set.set("exitY", parseInteger(child.getAttributes(), "y"));
	                    set.set("exitZ", parseInteger(child.getAttributes(), "z"));
	                    break;

	                case "price":
	                    set.set("priceItemId", parseInteger(child.getAttributes(), "itemId"));
	                    set.set("pricePerMob", parseInteger(child.getAttributes(), "countPerMob"));
	                    break;

	                case "limits":
	                    set.set("minBuy", parseInteger(child.getAttributes(), "minBuy"));
	                    set.set("maxBuy", parseInteger(child.getAttributes(), "maxBuy"));
	                    set.set("customBuy", parseBoolean(child.getAttributes(), "customBuy"));
	                    break;

	                case "timer":
	                    set.set("instanceMinutes", parseInteger(child.getAttributes(), "instanceMinutes"));
	                    break;

	                case "spawnControl":
	                    set.set("maxAliveMonsters", parseInteger(child.getAttributes(), "maxAliveMonsters"));
	                    set.set("respawnOnKillDelay", parseInteger(child.getAttributes(), "respawnOnKillDelay"));
	                    break;

	                case "rewards":
	                    forEach(child, "reward", rewardNode ->
	                    {
	                        rewards.add(new SoloFarmReward(
	                            parseInteger(rewardNode.getAttributes(), "itemId"),
	                            parseInteger(rewardNode.getAttributes(), "min"),
	                            parseInteger(rewardNode.getAttributes(), "max"),
	                            parseInteger(rewardNode.getAttributes(), "chance")
	                        ));
	                    });
	                    break;
	            }
	        });

	        set.set("rewards", rewards);

	        _config = new SoloFarmConfig(set);
	    });
	}
	public SoloFarmConfig getConfig()
	{
		return _config;
	}
	
	public static SoloFarmData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		private static final SoloFarmData INSTANCE = new SoloFarmData();
	}
}