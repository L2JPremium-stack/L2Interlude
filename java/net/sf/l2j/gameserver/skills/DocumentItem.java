package net.sf.l2j.gameserver.skills;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.sf.l2j.event.partyfarm.RewardHolder;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.skills.conditions.Condition;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author mkizub, JIV
 */
public final class DocumentItem extends DocumentBase
{
	public class NewItem
	{
		public int id;
		public String type;
		public String name;
		public StatsSet set;
		public int currentLevel;
		public Item item;
	}
	
	private NewItem _currentItem;
	private final List<Item> _itemsInFile = new ArrayList<>();
	
	public DocumentItem(File file)
	{
		super(file);
	}
	
	@Override
	protected StatsSet getStatsSet()
	{
		return _currentItem.set;
	}
	
	@Override
	protected String getTableValue(String name)
	{
		return _tables.get(name)[_currentItem.currentLevel];
	}
	
	@Override
	protected String getTableValue(String name, int idx)
	{
		return _tables.get(name)[idx - 1];
	}
	
	@Override
	protected void parseDocument(Document doc)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						try
						{
							_currentItem = new NewItem();
							parseItem(d);
							_itemsInFile.add(_currentItem.item);
							resetTable();
						}
						catch (Exception e)
						{
							_log.log(Level.WARNING, "Cannot create item " + _currentItem.id, e);
						}
					}
				}
			}
		}
	}
	
//	protected void parseItem(Node n) throws InvocationTargetException
//	{
//	    int itemId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
//	    String className = n.getAttributes().getNamedItem("type").getNodeValue();
//	    String itemName = n.getAttributes().getNamedItem("name").getNodeValue();
//
//	    _currentItem.id = itemId;
//	    _currentItem.name = itemName;
//	    _currentItem.type = className;
//	    _currentItem.set = new StatsSet();
//	    _currentItem.set.set("item_id", itemId);
//	    _currentItem.set.set("name", itemName);
//
//	    Node first = n.getFirstChild();
//
//	    // Variável para armazenar se o item já foi criado
//	    boolean itemCreated = false;
//
//	    for (n = first; n != null; n = n.getNextSibling())
//	    {
//	        if ("table".equalsIgnoreCase(n.getNodeName()))
//	        {
//	            if (itemCreated)
//	                throw new IllegalStateException("Item created but table node found! Item " + itemId);
//	            parseTable(n);
//	        }
//	        else if ("set".equalsIgnoreCase(n.getNodeName()))
//	        {
//	            if (itemCreated)
//	                throw new IllegalStateException("Item created but set node found! Item " + itemId);
//	            parseBeanSet(n, _currentItem.set, 1);
//	        }
//	        else if ("for".equalsIgnoreCase(n.getNodeName()))
//	        {
//	            if (!itemCreated)
//	            {
//	                makeItem();
//	                itemCreated = true;
//	            }
//	            parseTemplate(n, _currentItem.item);
//	        }
//	        else if ("reward".equalsIgnoreCase(n.getNodeName()))
//	        {
//	            // Se o item ainda não foi criado, cria antes
//	            if (!itemCreated)
//	            {
//	                makeItem();
//	                itemCreated = true;
//	            }
//
//	            int rewardItemId = Integer.parseInt(n.getAttributes().getNamedItem("itemId").getNodeValue());
//	            int min = Integer.parseInt(n.getAttributes().getNamedItem("min").getNodeValue());
//	            int max = Integer.parseInt(n.getAttributes().getNamedItem("max").getNodeValue());
//	            int chance = Integer.parseInt(n.getAttributes().getNamedItem("chance").getNodeValue());
//
//	            _currentItem.item.addLuckBoxReward(new RewardHolder(rewardItemId, min, max, chance));
//	        }
//	        else if ("cond".equalsIgnoreCase(n.getNodeName()))
//	        {
//	            if (!itemCreated)
//	            {
//	                makeItem();
//	                itemCreated = true;
//	            }
//
//	            Condition condition = parseCondition(n.getFirstChild(), _currentItem.item);
//	            Node msg = n.getAttributes().getNamedItem("msg");
//	            Node msgId = n.getAttributes().getNamedItem("msgId");
//
//	            if (condition != null && msg != null)
//	                condition.setMessage(msg.getNodeValue());
//	            else if (condition != null && msgId != null)
//	            {
//	                condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
//	                Node addName = n.getAttributes().getNamedItem("addName");
//
//	                if (addName != null && Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)
//	                    condition.addName();
//	            }
//	            _currentItem.item.attach(condition);
//	        }
//	    }
//
//	    // Se o item ainda não foi criado no loop, cria ele agora
//	    if (!itemCreated)
//	    {
//	        makeItem();
//	        itemCreated = true;
//	    }
//	}
	protected void parseItem(Node n) throws InvocationTargetException
	{
	    int itemId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
	    String className = n.getAttributes().getNamedItem("type").getNodeValue();
	    String itemName = n.getAttributes().getNamedItem("name").getNodeValue();

	    _currentItem.id = itemId;
	    _currentItem.name = itemName;
	    _currentItem.type = className;
	    _currentItem.set = new StatsSet();
	    _currentItem.set.set("item_id", itemId);
	    _currentItem.set.set("name", itemName);

	    Node first = n.getFirstChild();

	    // Variável para armazenar se o item já foi criado
	    boolean itemCreated = false;

	    for (n = first; n != null; n = n.getNextSibling())
	    {
	        if ("table".equalsIgnoreCase(n.getNodeName()))
	        {
	            if (itemCreated)
	                throw new IllegalStateException("Item created but table node found! Item " + itemId);
	            parseTable(n);
	        }
	        else if ("set".equalsIgnoreCase(n.getNodeName()))
	        {
	            if (itemCreated)
	                throw new IllegalStateException("Item created but set node found! Item " + itemId);
	            parseBeanSet(n, _currentItem.set, 1);
	        }
	        else if ("for".equalsIgnoreCase(n.getNodeName()))
	        {
	            if (!itemCreated)
	            {
	                makeItem();
	                itemCreated = true;
	            }
	            parseTemplate(n, _currentItem.item);
	        }
	        else if ("reward".equalsIgnoreCase(n.getNodeName()))
	        {
	            // LuckyBox reward
	            if (!itemCreated)
	            {
	                makeItem();
	                itemCreated = true;
	            }

	            int rewardItemId = Integer.parseInt(n.getAttributes().getNamedItem("itemId").getNodeValue());
	            int min = Integer.parseInt(n.getAttributes().getNamedItem("min").getNodeValue());
	            int max = Integer.parseInt(n.getAttributes().getNamedItem("max").getNodeValue());
	            int chance = Integer.parseInt(n.getAttributes().getNamedItem("chance").getNodeValue());

	            _currentItem.item.addLuckBoxReward(new RewardHolder(rewardItemId, min, max, chance));
	        }
	        else if ("boxRewards".equalsIgnoreCase(n.getNodeName()))
	        {
	            // BoxRewards fixo 100%
	            if (!itemCreated)
	            {
	                makeItem();
	                itemCreated = true;
	            }

	            for (Node rewardNode = n.getFirstChild(); rewardNode != null; rewardNode = rewardNode.getNextSibling())
	            {
	                if ("reward".equalsIgnoreCase(rewardNode.getNodeName()))
	                {
	                    int rewardId = Integer.parseInt(rewardNode.getAttributes().getNamedItem("id").getNodeValue());
	                    int amount = Integer.parseInt(rewardNode.getAttributes().getNamedItem("amount").getNodeValue());

	                    _currentItem.item.addBoxReward(new RewardHolder(rewardId, amount, amount, 100));
	                }
	            }
	        }
	        else if ("cond".equalsIgnoreCase(n.getNodeName()))
	        {
	            if (!itemCreated)
	            {
	                makeItem();
	                itemCreated = true;
	            }

	            Condition condition = parseCondition(n.getFirstChild(), _currentItem.item);
	            Node msg = n.getAttributes().getNamedItem("msg");
	            Node msgId = n.getAttributes().getNamedItem("msgId");

	            if (condition != null && msg != null)
	                condition.setMessage(msg.getNodeValue());
	            else if (condition != null && msgId != null)
	            {
	                condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
	                Node addName = n.getAttributes().getNamedItem("addName");

	                if (addName != null && Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)
	                    condition.addName();
	            }
	            _currentItem.item.attach(condition);
	        }
	    }

	    // Se o item ainda não foi criado no loop, cria ele agora
	    if (!itemCreated)
	    {
	        makeItem();
	        itemCreated = true;
	    }
	}
	
	private void makeItem() throws InvocationTargetException
	{
		if (_currentItem.item != null)
			return; // item is already created
		try
		{
			Constructor<?> c = Class.forName("net.sf.l2j.gameserver.model.item.kind." + _currentItem.type).getConstructor(StatsSet.class);
			_currentItem.item = (Item) c.newInstance(_currentItem.set);
		}
		catch (Exception e)
		{
			throw new InvocationTargetException(e);
		}
	}

	public List<Item> getItemList()
	{
		return _itemsInFile;
	}
}