package net.sf.l2j.gameserver.balance.skills;

import java.nio.file.Path;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

public class SkillBalanceReader implements IXmlReader
{
	@Override
	public void load()
	{
		parseFile("./data/balance/skills");
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc.getDocumentElement(), "skill", skillNode -> {
			NamedNodeMap attrs = skillNode.getAttributes();
			StatsSet set = new StatsSet();
			
			addAttributes(set, attrs);
			
			forEach(skillNode, "balance", balNode -> addAttributes(set, balNode.getAttributes()));
			
			SkillBalanceHolder.add(new SkillBalance(set));
		});
	}
}
