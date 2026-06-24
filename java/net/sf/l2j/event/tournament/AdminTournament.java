package net.sf.l2j.event.tournament;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

public class AdminTournament implements IAdminCommandHandler
{
	
	protected static final Logger _log = Logger.getLogger(AdminTournament.class.getName());
	public static boolean _arena_manual = false;
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equals("admin_tour"))
		{
			if (ArenaTask._started)
			{
				_log.info("----------------------------------------------------------------------------");
				_log.info("[Tournament]: Event Finished.");
				_log.info("----------------------------------------------------------------------------");
				ArenaTask._aborted = true;
				finishEventArena();
				_arena_manual = true;
				
				activeChar.sendMessage("SYS: Voce Finalizou o evento Tournament Manualmente..");
			}
			else
			{
				_log.info("----------------------------------------------------------------------------");
				_log.info("[Tournament]: Event Started.");
				_log.info("----------------------------------------------------------------------------");
				initEventArena();
				_arena_manual = true;
				activeChar.sendMessage("SYS: Voce ativou o evento Tournament Manualmente..");
			}
		}
		return false;
	}

	@Override
	public String[] getAdminCommandList() {
		
		return new String[] {"admin_tour"};
	}
	private static void initEventArena()
	{
		ThreadPool.schedule(new Runnable()
		{
			@Override
			public void run()
			{
				
				ArenaTask.SpawnEvent();
			}
		}, 1L);
	}
	
	private static void finishEventArena()
	{
		ThreadPool.schedule(new Runnable()
		{
			@Override
			public void run()
			{
				
				ArenaTask.finishEvent();
			}
		}, 1L);
	}

}
