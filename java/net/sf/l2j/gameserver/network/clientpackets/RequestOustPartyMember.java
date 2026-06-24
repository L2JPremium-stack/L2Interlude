package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.event.tournament.Arena3x3;
import net.sf.l2j.event.tournament.Arena5x5;
import net.sf.l2j.event.tournament.Arena9x9;
import net.sf.l2j.gameserver.model.L2Party.MessageType;
import net.sf.l2j.gameserver.model.actor.Player;

public final class RequestOustPartyMember extends L2GameClientPacket
{
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (activeChar.isInParty() && activeChar.getParty().isLeader(activeChar))
		{
			if (activeChar.isInArenaEvent() || Arena3x3.getInstance().isRegistered(activeChar) || Arena5x5.getInstance().isRegistered(activeChar) || Arena9x9.getInstance().isRegistered(activeChar))
				activeChar.sendMessage("You can't dismiss party member when you are in Tournament Event.");
			else if (activeChar.getParty().isInDimensionalRift() && !activeChar.getParty().getDimensionalRift().getRevivedAtWaitingRoom().contains(activeChar))
				activeChar.sendMessage("You can't dismiss party member when you are in Dimensional Rift.");
			else
				activeChar.getParty().removePartyMember(_name, MessageType.Expelled);
		}
	}
}