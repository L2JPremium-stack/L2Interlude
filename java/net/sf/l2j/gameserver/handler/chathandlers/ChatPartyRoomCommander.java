package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class ChatPartyRoomCommander implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		15
	};
	
	@Override
	public void handleChat(int type, Player activeChar, String target, String text)
	{
		if (!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel() || !activeChar.getParty().getCommandChannel().getChannelLeader().equals(activeChar))
			return;
		activeChar.getParty().getCommandChannel().broadcastCSToChannelMembers(new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text), activeChar);
	}
	
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}
