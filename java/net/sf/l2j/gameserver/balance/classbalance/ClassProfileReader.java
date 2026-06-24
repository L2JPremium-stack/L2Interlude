package net.sf.l2j.gameserver.balance.classbalance;

import java.nio.file.Path;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

public class ClassProfileReader implements IXmlReader
{
	@Override
	public void load()
	{
		parseFile("./data/balance/class");
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc.getDocumentElement(), "class", classNode -> {
			NamedNodeMap attrs = classNode.getAttributes();
			StatsSet set = new StatsSet();
			
			addAttributes(set, attrs);
			
			forEach(classNode, "stats", statNode -> addAttributes(set, statNode.getAttributes()));
			
			ClassProfileHolder.add(new ClassProfile(set));
		});
	}
}
