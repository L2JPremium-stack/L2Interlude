package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.model.actor.Player;

public interface IChatHandler
{
	public void handleChat(int type, Player activeChar, String target, String text);
	
	public int[] getChatTypeList();
}
