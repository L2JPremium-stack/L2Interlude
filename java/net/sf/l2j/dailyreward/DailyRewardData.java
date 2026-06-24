package net.sf.l2j.dailyreward;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.xmlfactory.XMLDocumentFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DailyRewardData
{
	private static final Logger _log = Logger.getLogger(DailyRewardData.class.getName());
	private static Map<Integer, DailyReward> dailyRewards = new LinkedHashMap<>();
	private Map<DailyReward, Set<Integer>> playersReceivedObjId = new LinkedHashMap<>();
	private Map<DailyReward, Set<String>> playersReceivedHIWD = new LinkedHashMap<>();
	public Map<Integer, DailyReward> getDailyRewards()
	{
		return dailyRewards;
	}

	public void setDailyRewards(Map<Integer, DailyReward> dailyRewards)
	{
	    DailyRewardData.dailyRewards = dailyRewards;
	}


	public DailyRewardData()
	{
		load();
	}
	
	public void  getRewardedPlayersObjectIds()
	{
		for(DailyReward dr : getAllDailyRewads())
		{
			playersReceivedObjId.put(dr, dr.getPlayersReceivdList());
		}
	}
	
	public void getRewardedPlayersHWIDs()
	{
		for(DailyReward dr : getAllDailyRewads())
		{
			playersReceivedHIWD.put(dr, dr.getHwidReceivedList());
		}
	}
	
	public void addReceivedPlayersToReward(DailyReward reward, Set<Integer> listObjId, Set<String> listHwids)
	{
		reward.setPlayersReceivdList(listObjId);
		reward.setHwidReceivedList(listHwids);
	}

	public void reload()
	{
		// armazena a lista de players
		playersReceivedObjId.clear();
		getRewardedPlayersObjectIds();
		playersReceivedHIWD.clear();
		getRewardedPlayersHWIDs();
		// destroi a lista de reward
		dailyRewards.clear();
		// cria nova lista de reward
		load();
		// adiciona nova lista de player a cada reward
		if(!playersReceivedObjId.isEmpty())
		{
			for(Map.Entry<DailyReward, Set<Integer>> entry : playersReceivedObjId.entrySet())
			{
				for(DailyReward dr : getAllDailyRewads())
				{
					if(dr.getDay() == entry.getKey().getDay())
					{
						dr.setPlayersReceivdList(entry.getValue());
						
					}
				}	
			}
			
		}
		if(!playersReceivedObjId.isEmpty())
		{
			for(Map.Entry<DailyReward, Set<String>> entry : playersReceivedHIWD.entrySet())
			{
				for(DailyReward dr : getAllDailyRewads())
				{
					if(dr.getDay() == entry.getKey().getDay())
					{
						dr.setHwidReceivedList(entry.getValue());
					}
				}	
			}
		}
	}

	private static class SingleTonHolder
	{
		protected static final DailyRewardData _instance = new DailyRewardData();
	}

	public static DailyRewardData getInstance()
	{
		return SingleTonHolder._instance;
	}

	private static void load()
	{
		//parseFile("./data/xml/DailyRewards.xml");
		try
		{

			File f = new File("./data/xml/custom/DailyRewards.xml");
			Document doc = XMLDocumentFactory.getInstance().loadDocument(f);
			
			// First element is never read.
			final Node n = doc.getFirstChild();

			for (Node o = n.getFirstChild(); o != null; o = o.getNextSibling())
			{
				if (!"reward".equalsIgnoreCase(o.getNodeName()))
					continue;

				NamedNodeMap attrs = o.getAttributes();
				DailyReward reward = null;
				final int day = Integer.parseInt(attrs.getNamedItem("Day").getNodeValue());

				for (Node d = o.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if (!"item".equalsIgnoreCase(d.getNodeName()))
						continue;

					attrs = d.getAttributes();

					final int itemId = Integer.parseInt(attrs.getNamedItem("itemId").getNodeValue());
					final int amount = Integer.parseInt(attrs.getNamedItem("amount").getNodeValue());
					final int enchantLevel = Integer.parseInt(attrs.getNamedItem("enchantLevel").getNodeValue());
					reward = new DailyReward(day, itemId);
					reward.setAmount(amount);
					reward.setEnchantLevel(enchantLevel);
					if (ItemTable.getInstance().getTemplate(itemId) != null)
					{
						dailyRewards.put(day, reward);
					}
					else
					{
						_log.warning("Daily Reward Data: Item ID: " + itemId + " doesn't exists in game.");
						
					}
				}

			}

		}
		catch (Exception e)
		{
			_log.warning("Daily Reward Data: Error while creating table: " + e);
		}

	}

	public DailyReward getDailyRewardByDay(int day)
	{
		return dailyRewards.get(day);
	}
	public List<DailyReward> getAllDailyRewads()
	{
	    return new ArrayList<>(dailyRewards.values());
	}

}
