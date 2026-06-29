package net.sf.l2j.launcher.drops;

public class NpcDrop
{
	public int itemId;
	public int min;
	public int max;
	public long chance;
	
	public NpcDrop()
	{
	}
	
	public NpcDrop(int itemId, int min, int max, long chance)
	{
		this.itemId = itemId;
		this.min = min;
		this.max = max;
		this.chance = chance;
	}
}
