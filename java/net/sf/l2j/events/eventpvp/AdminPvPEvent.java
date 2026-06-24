package net.sf.l2j.events.eventpvp;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

/**
 * 
 * @author Sarada
 *
 */

public class AdminPvPEvent implements IAdminCommandHandler
{
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_pvpevent"
	};
	
	protected static final Logger _log = Logger.getLogger(AdminPvPEvent.class.getName());
	public static boolean _bestfarm_manual = false;
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		
		if (command.equals("admin_pvpevent"))
		{
			if(PvPEvent.getInstance().isActive())
			{
				activeChar.sendMessage("Nao e possivel iniciar porque o Evento ja esta ativo.");
				return false;
			}
			
			_log.info("----------------------------------------------------------------------------");
			_log.info("[PvP Event]: Event Started.");
			_log.info("----------------------------------------------------------------------------");
			initEventPvPEvent();
			_bestfarm_manual = true;
			activeChar.sendMessage("SYS: Voce ativou o PvP Event Manualmente..");
		}
		return true;
	}
	private static void initEventPvPEvent()
	{
		ThreadPool.schedule(new Runnable()
		{
			@Override
			public void run()
			{
				
				PvPEventManager.getInstance().startEvent();
			}
		}, 1L);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}