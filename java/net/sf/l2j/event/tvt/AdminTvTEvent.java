package net.sf.l2j.event.tvt;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.Player;

public class AdminTvTEvent implements IAdminCommandHandler
{
    private static final String[] ADMIN_COMMANDS =
    {
        "admin_tvt_add",
        "admin_tvt_remove",
        "admin_tvt_advance"
    };
   
    @Override
	public boolean useAdminCommand(String command, Player activeChar)
    {
        if (command.startsWith("admin_tvt_add"))
        {
            L2Object target = activeChar.getTarget();
           
            if (!(target instanceof Player))
            {
                activeChar.sendMessage("You should select a player!");
                return true;
            }
           
            add(activeChar, (Player) target);
        }
        else if (command.startsWith("admin_tvt_remove"))
        {
            L2Object target = activeChar.getTarget();
           
            if (!(target instanceof Player))
            {
                activeChar.sendMessage("You should select a player!");
                return true;
            }
           
            remove(activeChar, (Player) target);
        }
        else if (command.startsWith( "admin_tvt_advance" ))
        {
            TvTManager.getInstance().skipDelay();
        }
       
        return true;
    }
   
    @Override
	public String[] getAdminCommandList()
    {
        return ADMIN_COMMANDS;
    }
   
    private static void add(Player activeChar, Player playerInstance)
    {
        if (TvTEvent.isPlayerParticipant(playerInstance.getObjectId()))
        {
            activeChar.sendMessage("Player already participated in the event!");
            return;
        }
       
        if (!TvTEvent.addParticipant(playerInstance))
        {
            activeChar.sendMessage("Player instance could not be added, it seems to be null!");
            return;
        }
       
        if (TvTEvent.isStarted())
        {
            new TvTEventTeleporter(playerInstance, TvTEvent.getParticipantTeamCoordinates(playerInstance.getObjectId()), true, false, TvTEvent.getObjects());
        }
    }
   
    private static void remove(Player activeChar, Player playerInstance)
    {
        if (!TvTEvent.removeParticipant(playerInstance.getObjectId()))
        {
            activeChar.sendMessage("Player is not part of the event!");
            return;
        }
       
        new TvTEventTeleporter(playerInstance, TvTConfig.TVT_EVENT_PARTICIPATION_NPC_COORDINATES, true, true, TvTEvent.getObjects());
    }
}