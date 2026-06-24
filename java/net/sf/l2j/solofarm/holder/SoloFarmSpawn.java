package net.sf.l2j.solofarm.holder;

public class SoloFarmSpawn
{
	private final int _npcId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	private final int _respawnDelay;
	private final boolean _boss;
	
	public SoloFarmSpawn(int npcId, int x, int y, int z, int heading, int respawnDelay, boolean boss)
	{
		_npcId = npcId;
		_x = x;
		_y = y;
		_z = z;
		_heading = heading;
		_respawnDelay = respawnDelay;
		_boss = boss;
	}
	
	public int getNpcId()
	{
		return _npcId;
	}
	
	public int getX()
	{
		return _x;
	}
	
	public int getY()
	{
		return _y;
	}
	
	public int getZ()
	{
		return _z;
	}
	
	public int getHeading()
	{
		return _heading;
	}
	
	public int getRespawnDelay()
	{
		return _respawnDelay;
	}
	
	public boolean isBoss()
	{
		return _boss;
	}
}
