package net.sf.l2j.AutoFarm;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.handler.tutorialhandlers.Autofarm;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedMenu;
import net.sf.l2j.gameserver.model.actor.Player;

public class VoicedAutofarm implements IVoicedCommandHandler 
{
    private final String[] VOICED_COMMANDS = 
    {
    	"autofarm"
    };

    @Override
    public boolean useVoicedCommand(final String command, final Player player, final String args)
    {
    	if (command.startsWith("autofarm"))
    	{
    		if(Config.ENABLE_COMMAND_VIP_AUTOFARM)
        	{
        		if(!player.isVip())
        		{
        			VoicedMenu.showMenuHtml(player);
        			player.sendMessage("You are not VIP member.");
        			return false;
        		}
        	}
    		
    		Autofarm.showAutoFarm(player);
    	}
        return false;
    }
    
    @Override
    public String[] getVoicedCommandList() 
    {
        return VOICED_COMMANDS;
    }
}