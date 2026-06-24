package net.sf.l2j.gameserver.balance.matchup;

import java.nio.file.Path;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;

public final class MatchupReader implements IXmlReader
{
	@Override
	public void load()
	{
		parseFile("./data/balance/matchup");
	}
	
	public void reload()
	{
		load();
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "matchup", node -> {
			StatsSet set = new StatsSet();
			
			set.set("attacker", parseInt(node));
			set.set("target", parseInt(node));
			
			forEach(node, "modifiers", modNode -> addAttributes(set, modNode.getAttributes()));
			
			MatchupHolder.add(new MatchupProfile(set));
		});
	}
}
