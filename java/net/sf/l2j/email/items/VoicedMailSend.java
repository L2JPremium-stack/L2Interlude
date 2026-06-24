package net.sf.l2j.email.items;

import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class VoicedMailSend implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands = { "email" };

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (command.equalsIgnoreCase("email"))
		{
			showTargetInput(activeChar);
			return true;
		}
		return false;
	}

	public static void showTargetInput(Player player)
	{
	    NpcHtmlMessage html = new NpcHtmlMessage(player.getObjectId());
	    StringBuilder sb = new StringBuilder();

	    sb.append("<html><body>");
	    sb.append("<center>");

	    sb.append("<font color=LEVEL><b>Send Mail</b></font><br><br>");

	    sb.append("Enter player name:<br>");
	    sb.append("<edit var=\"target\" width=120 height=15 maxlen=32><br><br>");

	    sb.append("<a action=\"bypass -h email_send $target\">Next</a>");

	    sb.append("</center>");
	    sb.append("</body></html>");

	    html.setHtml(sb.toString());
	    player.sendPacket(html);
	}



	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}
