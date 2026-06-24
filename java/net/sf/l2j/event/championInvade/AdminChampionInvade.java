package net.sf.l2j.event.championInvade;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

public class AdminChampionInvade implements IAdminCommandHandler
{
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_championinvade"
	};
	
	protected static final Logger _log = Logger.getLogger(AdminChampionInvade.class.getName());
	public static boolean _bestfarm_manual = false;
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		
		if (command.equals("admin_championinvade"))
		{
			if (ChampionInvade._started)
			{
				_log.info("----------------------------------------------------------------------------");
				_log.info("[Champion Invade Event]: Event Finished.");
				_log.info("----------------------------------------------------------------------------");
				ChampionInvade._aborted = true;
				finishEventSpecialChampion();
				
				activeChar.sendMessage("SYS: Voce Finalizou o Champion Invade Event Manualmente..");
			}
			else
			{
				_log.info("----------------------------------------------------------------------------");
				_log.info("[Champion Invade Event]: Event Started.");
				_log.info("----------------------------------------------------------------------------");
				initEventChampionEvent();
				_bestfarm_manual = true;
				activeChar.sendMessage("SYS: Voce ativou o Champion Invade Event Manualmente..");
			}
		}
		return true;
		
	}
	
	private static void initEventChampionEvent()
	{
		ThreadPool.schedule(new Runnable()
		{
			@Override
			public void run()
			{
				
				ChampionInvade.StartedEvent();
			}
		}, 1L);
	}
	
	private static void finishEventSpecialChampion()
	{
		ThreadPool.schedule(new Runnable()
		{
			@Override
			public void run()
			{
				ChampionInvade.Finish_Event();
				
			}
		}, 1L);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
