package net.sf.l2j.itemstime;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TimeItemData
{
	private static final Logger _log = Logger.getLogger(TimeItemData.class.getName());
	private final Map<Integer, Long> _itemDurations = new HashMap<>();

	protected TimeItemData()
	{
		load();
	}

	private void load()
	{
		try
		{
			File file = new File("./data/xml/custom/TimeItems.xml");
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);

			NodeList list = doc.getElementsByTagName("item");
			for (int i = 0; i < list.getLength(); i++)
			{
				Node node = list.item(i);
				NamedNodeMap attrs = node.getAttributes();

				int itemId = Integer.parseInt(attrs.getNamedItem("itemId").getNodeValue());
				String timeStr = attrs.getNamedItem("time").getNodeValue();

				long duration = parseTimeString(timeStr);
				_itemDurations.put(itemId, duration);
			}

			_log.info("TimeItemData: Carregados " + _itemDurations.size() + " itens temporarios.");
		}
		catch (Exception e)
		{
			_log.warning("TimeItemData: Erro ao carregar TimeItems.xml: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static long parseTimeString(String input)
	{
		input = input.toLowerCase().trim();

		if (input.endsWith("hour"))
			return Long.parseLong(input.replace("hour", "").trim()) * 60 * 60;
		if (input.endsWith("day"))
			return Long.parseLong(input.replace("day", "").trim()) * 24 * 60 * 60;
		if (input.endsWith("minute") || input.endsWith("min"))
			return Long.parseLong(input.replace("minute", "").replace("min", "").trim()) * 60;

		return 3600; // Default: 1h
	}

	public boolean isTimedItem(int itemId)
	{
		return _itemDurations.containsKey(itemId);
	}

	public long getDuration(int itemId)
	{
		return _itemDurations.getOrDefault(itemId, 0L);
	}

	public static TimeItemData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final TimeItemData INSTANCE = new TimeItemData();
	}
}
