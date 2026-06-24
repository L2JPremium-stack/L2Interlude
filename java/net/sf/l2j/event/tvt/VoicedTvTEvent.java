package net.sf.l2j.event.tvt;

import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class VoicedTvTEvent implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"tvtinfo",
		"tvtjoin",
		"tvtleave",
	};

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		
		
		if (command.equals("tvtinfo"))
		{
			if (TvTEvent.isStarting() || TvTEvent.isStarted())
			{
				showTvTStatuPage(activeChar);
				return true;
			}
			activeChar.sendMessage("Team vs Team fight is not in progress.");
			return false;
		}
		else if (command.equals("tvtjoin"))
		{
			if (!TvTEvent.isPlayerParticipant(activeChar.getObjectId()))
			{
				TvTEvent.onBypass("tvt_event_participation", activeChar);
			}
			else
			{
				activeChar.sendMessage("You are already registered.");
				return false;
			}
		}
		else if (command.equals("tvtleave"))
		{
			if (TvTEvent.isPlayerParticipant(activeChar.getObjectId()))
			{
				TvTEvent.onBypass("tvt_event_remove_participation", activeChar);
			}
			else
			{
				activeChar.sendMessage("You are not registered.");
				return false;
			}
		}
		
		return true;
	}

	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
//	private static void showTvTStatuPage(L2PcInstance activeChar)
//	{
//	    NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
//	    html.setFile("data/html/mods/events/tvt/Status.htm");
//
//	    TvTAreasLoader.Area chosenArea = TvTEvent.getChosenArea();  // ou TvTEvent._chosenArea se for público e estático
//
//	    String team1Name = (chosenArea != null) ? chosenArea.team1Name : "Team 1";
//	    String team2Name = (chosenArea != null) ? chosenArea.team2Name : "Team 2";
//
//	    html.replace("%team1name%", team1Name);
//	    html.replace("%team1playercount%", String.valueOf(TvTEvent.getTeamsPlayerCounts()[0]));
//	    html.replace("%team1points%", String.valueOf(TvTEvent.getTeamsPoints()[0]));
//	    html.replace("%team2name%", team2Name);
//	    html.replace("%team2playercount%", String.valueOf(TvTEvent.getTeamsPlayerCounts()[1]));
//	    html.replace("%team2points%", String.valueOf(TvTEvent.getTeamsPoints()[1]));
//	    activeChar.sendPacket(html);
//	}
	private static void showTvTStatuPage(Player activeChar)
	{
	    NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
	    html.setFile("data/html/mods/events/tvt/Status.htm");

	    TvTAreasLoader.Area chosenArea = TvTEvent.getChosenArea();

	    // Pega os nomes dos times da área, se disponível
	    String team1Name = (chosenArea != null) ? chosenArea.team1Name : "Team 1";
	    String team2Name = (chosenArea != null) ? chosenArea.team2Name : "Team 2";

	    // Inversão proposital para corrigir cores/posição no HTML
	    html.replace("%team1name%", team2Name); // <- invertido
	    html.replace("%team1playercount%", String.valueOf(TvTEvent.getTeamsPlayerCounts()[1]));
	    html.replace("%team1points%", String.valueOf(TvTEvent.getTeamsPoints()[1]));

	    html.replace("%team2name%", team1Name); // <- invertido
	    html.replace("%team2playercount%", String.valueOf(TvTEvent.getTeamsPlayerCounts()[0]));
	    html.replace("%team2points%", String.valueOf(TvTEvent.getTeamsPoints()[0]));

	    activeChar.sendPacket(html);
	}


}