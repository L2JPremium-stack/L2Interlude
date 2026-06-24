package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.L2CommandChannel;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ExMultiPartyCommandChannelInfo;

/**
 * @author chris_00 when User press the "List Update" button in CCInfo window
 */
public class ChannelListUpdate implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		97
	};
	
	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if (!activeChar.isInParty())
			return false;
		
		final L2CommandChannel channel = activeChar.getParty().getCommandChannel();
		if (channel == null)
			return false;
		
		activeChar.sendPacket(new ExMultiPartyCommandChannelInfo(channel));
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}