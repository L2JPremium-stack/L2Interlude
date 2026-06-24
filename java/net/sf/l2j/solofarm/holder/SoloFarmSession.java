package net.sf.l2j.solofarm.holder;

import java.util.HashSet;
import java.util.Set;

public class SoloFarmSession
{
	private final int _objectId;
	private int _instanceWorldId;
	private int _remainingToKill;
	private int _aliveMonsters;
	private long _endTime;
	
	private final Set<Integer> _spawnedNpcObjectIds = new HashSet<>();
	
	public SoloFarmSession(int objectId)
	{
		_objectId = objectId;
	}
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	public int getInstanceWorldId()
	{
		return _instanceWorldId;
	}
	
	public void setInstanceWorldId(int instanceWorldId)
	{
		_instanceWorldId = instanceWorldId;
	}
	
	public int getRemainingToKill()
	{
		return _remainingToKill;
	}
	
	public void setRemainingToKill(int remainingToKill)
	{
		_remainingToKill = remainingToKill;
	}
	
	public void decreaseRemaining()
	{
		_remainingToKill--;
	}
	
	public int getAliveMonsters()
	{
		return _aliveMonsters;
	}
	
	public void increaseAlive()
	{
		_aliveMonsters++;
	}
	
	public void decreaseAlive()
	{
		if (_aliveMonsters > 0)
			_aliveMonsters--;
	}
	
	public long getEndTime()
	{
		return _endTime;
	}
	
	public void setEndTime(long endTime)
	{
		_endTime = endTime;
	}
	
	public Set<Integer> getSpawnedNpcObjectIds()
	{
		return _spawnedNpcObjectIds;
	}
}
