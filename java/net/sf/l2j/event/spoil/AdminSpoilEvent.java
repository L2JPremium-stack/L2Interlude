package net.sf.l2j.event.spoil;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

public class AdminSpoilEvent implements IAdminCommandHandler
{
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_spoilevent"
	};
	
	protected static final Logger _log = Logger.getLogger(AdminSpoilEvent.class.getName());
	public static boolean _bestfarm_manual = false;
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		
		if (command.equals("admin_spoilevent"))
		{
			if (SpoilEvent._started)
			{
				_log.info("----------------------------------------------------------------------------");
				_log.info("[Spoil Event]: Event Finished.");
				_log.info("----------------------------------------------------------------------------");
				SpoilEvent._aborted = true;
				finishEventPartyFarm();
				
				activeChar.sendMessage("SYS: Voce Finalizou o Spoil Event Manualmente..");
			}
			else
			{
				_log.info("----------------------------------------------------------------------------");
				_log.info("[Spoil Event]: Event Started.");
				_log.info("----------------------------------------------------------------------------");
				initEventPartyFarm();
				_bestfarm_manual = true;
				activeChar.sendMessage("SYS: Voce ativou o Spoil Event Manualmente..");
			}
		}
		return true;
		
	}
	
	private static void initEventPartyFarm()
	{
		ThreadPool.schedule(new Runnable()
		{
			@Override
			public void run()
			{
				
				SpoilEvent.bossSpawnMonster();
			}
		}, 1L);
	}
	
	private static void finishEventPartyFarm()
	{
		ThreadPool.schedule(new Runnable()
		{
			@Override
			public void run()
			{
				
				SpoilEvent.Finish_Event();
				
			}
		}, 1L);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}