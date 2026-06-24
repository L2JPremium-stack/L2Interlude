package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Object.PolyType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Vehicle;
import net.sf.l2j.gameserver.network.serverpackets.DeleteObject;
import net.sf.l2j.gameserver.network.serverpackets.SpawnItem;

public class PcKnownList extends CharKnownList
{
	public PcKnownList(Player activeChar)
	{
		super(activeChar);
	}
	
	/**
	 * Add a visible L2Object to L2PcInstance _knownObjects and _knownPlayer (if necessary) and send Server-Client Packets needed to inform the L2PcInstance of its state and actions in progress.<BR>
	 * <BR>
	 * <B><U> object is a ItemInstance </U> :</B><BR>
	 * <BR>
	 * <li>Send Server-Client Packet DropItem/SpawnItem to the L2PcInstance</li><BR>
	 * <BR>
	 * <B><U> object is a L2DoorInstance </U> :</B><BR>
	 * <BR>
	 * <li>Send Server-Client Packets DoorInfo and DoorStatusUpdate to the L2PcInstance</li> <li>Send Server->Client packet MoveToPawn/MoveToLocation and AutoAttackStart to the L2PcInstance</li><BR>
	 * <BR>
	 * <B><U> object is a L2Npc </U> :</B><BR>
	 * <BR>
	 * <li>Send Server-Client Packet NpcInfo to the L2PcInstance</li> <li>Send Server->Client packet MoveToPawn/MoveToLocation and AutoAttackStart to the L2PcInstance</li><BR>
	 * <BR>
	 * <B><U> object is a L2Summon </U> :</B><BR>
	 * <BR>
	 * <li>Send Server-Client Packet NpcInfo/PetItemList (if the L2PcInstance is the owner) to the L2PcInstance</li> <li>Send Server->Client packet MoveToPawn/MoveToLocation and AutoAttackStart to the L2PcInstance</li><BR>
	 * <BR>
	 * <B><U> object is a L2PcInstance </U> :</B><BR>
	 * <BR>
	 * <li>Send Server-Client Packet CharInfo to the L2PcInstance</li> <li>If the object has a private store, Send Server-Client Packet PrivateStoreMsgSell to the L2PcInstance</li> <li>Send Server->Client packet MoveToPawn/MoveToLocation and AutoAttackStart to the L2PcInstance</li><BR>
	 * <BR>
	 * @param object The L2Object to add to _knownObjects and _knownPlayer
	 */
	@Override
	public boolean addKnownObject(L2Object object)
	{
		if (!super.addKnownObject(object))
			return false;
		
		sendInfoFrom(object);
		return true;
	}
	
	/**
	 * Remove a L2Object from L2PcInstance _knownObjects and _knownPlayer (if necessary) and send Server-Client Packet DeleteObject to the L2PcInstance.<BR>
	 * <BR>
	 * @param object The L2Object to remove from _knownObjects and _knownPlayer
	 */
	@Override
	public boolean removeKnownObject(L2Object object)
	{
		if (!super.removeKnownObject(object))
			return false;
		
		// get player
		final Player player = (Player) _activeObject;
		
		// send Server-Client Packet DeleteObject to the L2PcInstance
		player.sendPacket(new DeleteObject(object, (object instanceof Player) && ((Player) object).isSeated()));
		return true;
	}
	
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		if (object instanceof Vehicle)
			return 9000;
		
		final int knownlistSize = getKnownObjects().size();
		if (knownlistSize <= 25)
			return 3400;
		
		if (knownlistSize <= 35)
			return 2900;
		
		if (knownlistSize <= 70)
			return 2300;
		
		return 1700;
	}
	
	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		if (object instanceof Vehicle)
			return 10000;
		
		final int knownlistSize = getKnownObjects().size();
		if (knownlistSize <= 25)
			return 4000;
		
		if (knownlistSize <= 35)
			return 3500;
		
		if (knownlistSize <= 70)
			return 2910;
		
		return 2310;
	}
	
	public final void refreshInfos()
	{
	    for (L2Object object : _knownObjects.values())
	    {
	        if (object instanceof Player)
	        {
	        	Player player = (Player) object;
	        	
	            if (player.inObserverMode())
	                continue;
	            
	            if (object.getInstance() != player.getInstance()) 
	                continue;
	        }
	        
	        sendInfoFrom(object);
	    }
	}

	
	private final void sendInfoFrom(L2Object object)
	{
		// get player
		final Player player = (Player) _activeObject;
		
		if (object.getPolyType() == PolyType.ITEM)
			player.sendPacket(new SpawnItem(object));
		else
		{
			// send object info to player
			object.sendInfo(player);
			
			if (object instanceof Creature)
			{
				// Update the state of the L2Character object client side by sending Server->Client packet MoveToPawn/MoveToLocation and AutoAttackStart to the L2PcInstance
				Creature obj = (Creature) object;
				if (obj.hasAI())
					obj.getAI().describeStateToPlayer(player);
			}
		}
	}
}