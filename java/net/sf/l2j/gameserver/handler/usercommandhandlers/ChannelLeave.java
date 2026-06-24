package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.L2CommandChannel;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class ChannelLeave implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		96
	};
	
	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		final L2Party party = activeChar.getParty();
		if (party != null)
		{
			if (party.isLeader(activeChar) && party.isInCommandChannel())
			{
				final L2CommandChannel channel = party.getCommandChannel();
				channel.removeParty(party);
				
				party.getLeader().sendPacket(SystemMessageId.LEFT_COMMAND_CHANNEL);
				channel.broadcastToChannelMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_PARTY_LEFT_COMMAND_CHANNEL).addPcName(party.getLeader()));
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}