package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;

public class VehicleKnownList extends CharKnownList
{
	public VehicleKnownList(Creature activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		if (!(object instanceof Player))
			return 0;
		
		return object.getKnownList().getDistanceToWatchObject(_activeObject);
	}
	
	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		if (!(object instanceof Player))
			return 0;
		
		return object.getKnownList().getDistanceToForgetObject(_activeObject);
	}
}