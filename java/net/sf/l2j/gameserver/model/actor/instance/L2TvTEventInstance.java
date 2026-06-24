package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.event.tvt.TvTAreasLoader;
import net.sf.l2j.event.tvt.TvTEvent;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class L2TvTEventInstance extends L2NpcInstance
{
	private static final String TvthtmlPath = "data/html/mods/events/tvt/";
	
	public L2TvTEventInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	@Override
	public void showChatWindow(Player playerInstance, int val)
	{
	    if (playerInstance == null)
	        return;

	    TvTAreasLoader.Area chosenArea = TvTEvent.getChosenArea(); // ou TvTEvent._chosenArea

	    String team1Name = (chosenArea != null) ? chosenArea.team1Name : "Team 1";
	    String team2Name = (chosenArea != null) ? chosenArea.team2Name : "Team 2";

	    if (TvTEvent.isParticipating())
	    {
	        final boolean isParticipant = TvTEvent.isPlayerParticipant(playerInstance.getObjectId());
	        final String htmContent;

	        if (!isParticipant)
	            htmContent = HtmCache.getInstance().getHtm(TvthtmlPath + "Participation.htm");
	        else
	            htmContent = HtmCache.getInstance().getHtm(TvthtmlPath + "RemoveParticipation.htm");

	        if (htmContent != null)
	        {
	            int[] teamsPlayerCounts = TvTEvent.getTeamsPlayerCounts();
	            NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

	            npcHtmlMessage.setHtml(htmContent);
	            npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
	            npcHtmlMessage.replace("%team1name%", team1Name);
	            npcHtmlMessage.replace("%team1playercount%", String.valueOf(teamsPlayerCounts[0]));
	            npcHtmlMessage.replace("%team2name%", team2Name);
	            npcHtmlMessage.replace("%team2playercount%", String.valueOf(teamsPlayerCounts[1]));
	            npcHtmlMessage.replace("%playercount%", String.valueOf(teamsPlayerCounts[0] + teamsPlayerCounts[1]));
	            if (!isParticipant)
	            {
	                npcHtmlMessage.replace("%fee%", TvTEvent.getParticipationFee());
	            }

	            playerInstance.sendPacket(npcHtmlMessage);
	        }
	    }
	    else if (TvTEvent.isStarting() || TvTEvent.isStarted())
	    {
	        final String htmContent = HtmCache.getInstance().getHtm(TvthtmlPath + "Status.htm");

	        if (htmContent != null)
	        {
	            int[] teamsPlayerCounts = TvTEvent.getTeamsPlayerCounts();
	            int[] teamsPointsCounts = TvTEvent.getTeamsPoints();
	            NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

	            npcHtmlMessage.setHtml(htmContent);
	            npcHtmlMessage.replace("%team1name%", team1Name);
	            npcHtmlMessage.replace("%team1playercount%", String.valueOf(teamsPlayerCounts[0]));
	            npcHtmlMessage.replace("%team1points%", String.valueOf(teamsPointsCounts[0]));
	            npcHtmlMessage.replace("%team2name%", team2Name);
	            npcHtmlMessage.replace("%team2playercount%", String.valueOf(teamsPlayerCounts[1]));
	            npcHtmlMessage.replace("%team2points%", String.valueOf(teamsPointsCounts[1]));
	            playerInstance.sendPacket(npcHtmlMessage);
	        }
	    }

	    playerInstance.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onBypassFeedback(Player playerInstance, String command)
	{
		TvTEvent.onBypass(command, playerInstance);
	}
	
}