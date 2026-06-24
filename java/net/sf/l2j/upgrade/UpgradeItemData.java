package net.sf.l2j.upgrade;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

public class UpgradeItemData
{
	private final Map<Integer, UpgradeItem> _upgrades = new ConcurrentHashMap<>();

	private static final UpgradeItemData INSTANCE = new UpgradeItemData();

	public static UpgradeItemData getInstance()
	{
		return INSTANCE;
	}

	private UpgradeItemData()
	{
		load();
	}

	private void load()
	{
		try
		{
			File f = new File("./data/xml/custom/UpgradeItems.xml");
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);

			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("UpgradeItems".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("ItemUpgrade".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							StatsSet set = new StatsSet();

							for (int i = 0; i < attrs.getLength(); i++)
							{
								Node attr = attrs.item(i);
								set.set(attr.getNodeName(), attr.getNodeValue());
							}

							UpgradeItem item = new UpgradeItem(set);
							_upgrades.put(item.getItemId(), item);
						}
					}
				}
			}

			System.out.println("Loaded " + _upgrades.size() + " upgrade items.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public UpgradeItem getUpgrade(int itemId, int bossId)
	{
		UpgradeItem up = _upgrades.get(itemId);
		return (up != null && up.getBossId() == bossId) ? up : null;
	}
}
