package net.sf.l2j.launcher.drops;

import java.util.ArrayList;
import java.util.List;

public class DropCategory
{
	public int id;
	public List<NpcDrop> drops = new ArrayList<>();
	
	public DropCategory(int id)
	{
		this.id = id;
	}
}