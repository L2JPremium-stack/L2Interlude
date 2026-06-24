package net.sf.l2j.event.rewardsoloevent;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

public class AdminSoloEvent implements IAdminCommandHandler
{
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_soloevent"
	};
	
	protected static final Logger _log = Logger.getLogger(AdminSoloEvent.class.getName());
	public static boolean _bestfarm_manual = false;
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		
		if (command.equals("admin_soloevent"))
		{
			if (RewardSoloEvent._started)
			{
				_log.info("----------------------------------------------------------------------------");
				_log.info("[Reward Solo Event]: Event Finished.");
				_log.info("----------------------------------------------------------------------------");
				RewardSoloEvent._aborted = true;
				finishEventSpecialDrop();
				
				activeChar.sendMessage("SYS: Voce Finalizou o Reward Solo Event Manualmente..");
			}
			else
			{
				_log.info("----------------------------------------------------------------------------");
				_log.info("[Reward Solo Event]: Event Started.");
				_log.info("----------------------------------------------------------------------------");
				initEventSoloEvent();
				_bestfarm_manual = true;
				activeChar.sendMessage("SYS: Voce ativou o Reward Solo Event Manualmente..");
			}
		}
		return true;
		
	}
	
	private static void initEventSoloEvent()
	{
		ThreadPool.schedule(new Runnable()
		{
			@Override
			public void run()
			{
				
				RewardSoloEvent.StartedEvent();
			}
		}, 1L);
	}
	
	private static void finishEventSpecialDrop()
	{
		ThreadPool.schedule(new Runnable()
		{
			@Override
			public void run()
			{
				RewardSoloEvent.Finish_Event();
				
			}
		}, 1L);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
