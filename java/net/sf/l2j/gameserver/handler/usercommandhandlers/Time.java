package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.GameTimeController;

public class Time implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		77
	};
	
	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if (COMMAND_IDS[0] != id)
			return false;
		
		int t = GameTimeController.getInstance().getGameTime();
		int h = (t / 60) % 24;
		
		String m;
		if (t % 60 < 10)
			m = "0" + t % 60;
		else
			m = "" + t % 60;
		
		SystemMessage sm;
		
		if (GameTimeController.getInstance().isNight())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.TIME_S1_S2_IN_THE_NIGHT);
			sm.addNumber(h);
			sm.addString(m);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.TIME_S1_S2_IN_THE_DAY);
			sm.addNumber(h);
			sm.addString(m);
		}
		activeChar.sendPacket(sm);
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}