package net.sf.l2j.dailyreward;

import net.sf.l2j.gameserver.model.actor.Player;

public interface IBypassHandler
{
	public boolean handleBypass(String bypass, Player activeChar);
	
	public String[] getBypassHandlersList();
}