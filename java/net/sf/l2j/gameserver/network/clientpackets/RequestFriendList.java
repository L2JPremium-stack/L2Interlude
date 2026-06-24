package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.datatables.CharNameTable;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		SystemMessage sm;
		
		// ======<Friend List>======
		activeChar.sendPacket(SystemMessageId.FRIEND_LIST_HEADER);
		
		Player friend = null;
		for (int id : activeChar.getFriendList())
		{
			String friendName = CharNameTable.getInstance().getNameById(id);
			if (friendName == null)
				continue;
			
			friend = L2World.getInstance().getPlayer(friendName);
			
			// Currently offline
			if (friend == null || !friend.isOnline())
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_OFFLINE).addString(friendName);
			// Currently online
			else
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ONLINE).addString(friendName);
			
			activeChar.sendPacket(sm);
		}
		
		// =========================
		activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
	}
}