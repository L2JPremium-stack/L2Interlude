package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.event.tournament.Arena3x3;
import net.sf.l2j.event.tournament.Arena5x5;
import net.sf.l2j.event.tournament.Arena9x9;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Party.MessageType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoomList;
import net.sf.l2j.gameserver.network.serverpackets.ExClosePartyRoom;
import net.sf.l2j.gameserver.network.serverpackets.ExPartyRoomMember;
import net.sf.l2j.gameserver.network.serverpackets.PartyMatchDetail;

public final class RequestWithdrawParty extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (player.isArenaProtection())
		{
			player.sendMessage("You can't exit party in Tournament Event.");
			return;
		}
		
		final L2Party party = player.getParty();
		if (party == null)
			return;
		
		if (player.isInArenaEvent() || Arena3x3.getInstance().isRegistered(player) || Arena5x5.getInstance().isRegistered(player) || Arena9x9.getInstance().isRegistered(player))
			player.sendMessage("You can't exit party when you are in Tournament Event.");
		else if (party.isInDimensionalRift() && !party.getDimensionalRift().getRevivedAtWaitingRoom().contains(player))
			player.sendMessage("You can't exit party when you are in Dimensional Rift.");
		else
		{
			party.removePartyMember(player, MessageType.Left);
			
			if (player.isInPartyMatchRoom())
			{
				PartyMatchRoom _room = PartyMatchRoomList.getInstance().getPlayerRoom(player);
				if (_room != null)
				{
					player.sendPacket(new PartyMatchDetail(_room));
					player.sendPacket(new ExPartyRoomMember(_room, 0));
					player.sendPacket(ExClosePartyRoom.STATIC_PACKET);
					
					_room.deleteMember(player);
				}
				player.setPartyRoom(0);
				player.broadcastUserInfo();
			}
		}
	}
}