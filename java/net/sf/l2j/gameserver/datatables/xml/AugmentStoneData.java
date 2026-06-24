package net.sf.l2j.gameserver.datatables.xml;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.gameserver.model.holder.AugmentStoneHolder;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;

public class AugmentStoneData implements IXmlReader
{
	private final List<AugmentStoneHolder> _entries = new ArrayList<>();
	
	public AugmentStoneData()
	{
		load();
	}
	
	public void reload()
	{
		_entries.clear();
		load();
	}
	
	@Override
	public void load()
	{
		_entries.clear();
		parseFile("./data/xml/custom/augmentStones.xml");
		LOGGER.info("Loaded {" + _entries.size() + "} Augment Stones.");
	}
	
	@Override
	
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode -> {
			forEach(listNode, "augment", node -> {
				StatsSet set = parseAttributes(node);
				
				// ICON (caso use como tag)
				forEach(node, "icon", iconNode -> {
					set.set("icon", iconNode.getTextContent());
				});
				
				// SKILL
				forEach(node, "skill", skill -> {
					StatsSet s = parseAttributes(skill);
					set.set("skillId", s.getInteger("id"));
					set.set("skillLevel", s.getInteger("level", 1));
				});
				
				// PRICE
				forEach(node, "price", price -> {
					StatsSet p = parseAttributes(price);
					set.set("priceItemId", p.getInteger("itemId"));
					set.set("priceCount", p.getInteger("count"));
				});
				
				_entries.add(new AugmentStoneHolder(set));
			});
		});
	}
	
	public List<AugmentStoneHolder> getByType(String type)
	{
		return _entries.stream().filter(a -> a.getType().equalsIgnoreCase(type)).collect(Collectors.toList());
	}
	
	public static AugmentStoneData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AugmentStoneData INSTANCE = new AugmentStoneData();
	}
}