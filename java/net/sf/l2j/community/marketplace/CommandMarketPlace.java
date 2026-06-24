package net.sf.l2j.community.marketplace;

import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

public class CommandMarketPlace implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands = {"MarketPlace"};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String params)
	{
		if (command.equals("MarketPlace"))    
		{
			MarketplaceCBBypasses.showMarketBoard(activeChar, 1, "*null*");  
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}