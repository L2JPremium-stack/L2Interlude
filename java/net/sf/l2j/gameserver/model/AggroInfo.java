package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.model.actor.Creature;

/**
 * This class contains all AggroInfo of the L2Attackable against the attacker L2Character.
 * <ul>
 * <li>attacker : The attacker L2Character concerned by this AggroInfo of this L2Attackable</li>
 * <li>hate : Hate level of this L2Attackable against the attacker L2Character (hate = damage)</li>
 * <li>damage : Number of damages that the attacker L2Character gave to this L2Attackable</li>
 * </ul>
 */
public final class AggroInfo
{
	private final Creature _attacker;
	
	private int _hate;
	private int _damage;
	
	public AggroInfo(Creature attacker)
	{
		_attacker = attacker;
	}
	
	public Creature getAttacker()
	{
		return _attacker;
	}
	
	public int getHate()
	{
		return _hate;
	}
	
	public int checkHate(Creature owner)
	{
		if (_attacker.isAlikeDead() || !_attacker.isVisible() || !owner.getKnownList().knowsObject(_attacker))
			_hate = 0;
		
		return _hate;
	}
	
	public void addHate(int value)
	{
		_hate = (int) Math.min(_hate + (long) value, 999999999);
	}
	
	public void stopHate()
	{
		_hate = 0;
	}
	
	public int getDamage()
	{
		return _damage;
	}
	
	public void addDamage(int value)
	{
		_damage = (int) Math.min(_damage + (long) value, 999999999);
	}
	
	@Override
	public final boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		
		if (obj instanceof AggroInfo)
			return (((AggroInfo) obj).getAttacker() == _attacker);
		
		return false;
	}
	
	@Override
	public final int hashCode()
	{
		return _attacker.getObjectId();
	}
}
