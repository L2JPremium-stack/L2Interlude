package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeGuardInstance;

public class DoorKnownList extends CharKnownList
{
	public DoorKnownList(L2DoorInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		if (object instanceof L2SiegeGuardInstance)
			return 600;
		
		if (object instanceof Player)
			return 3500;
		
		return 0;
	}
	
	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		if (object instanceof L2SiegeGuardInstance)
			return 800;
		
		if (object instanceof Player)
			return 4000;
		
		return 0;
	}
}