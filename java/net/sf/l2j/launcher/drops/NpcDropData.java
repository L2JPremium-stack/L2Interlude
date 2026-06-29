package net.sf.l2j.launcher.drops;

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NpcDropData
{
	public int npcId;
	public String npcName;
	public String npcType;
	public File sourceFile;
	public Document document;
	public Element npcElement;
	public Element dropsElement;
	public boolean hadDropsBlock;
	public List<DropCategory> categories;
	
	public int getDropCount()
	{
		int count = 0;
		if (categories == null)
			return count;
		
		for (DropCategory category : categories)
		{
			if (category.drops != null)
				count += category.drops.size();
		}
		return count;
	}
}
