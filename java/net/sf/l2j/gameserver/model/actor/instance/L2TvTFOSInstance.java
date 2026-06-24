package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.event.fortress.FOSConfig;
import net.sf.l2j.event.fortress.FOSEvent;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class L2TvTFOSInstance extends L2NpcInstance
{
	private static final String foshtmlPath = "data/html/mods/events/fos/";
	
	public L2TvTFOSInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player playerInstance, String command)
	{
		FOSEvent.onBypass(command, playerInstance);
	}
	
	@Override
	public void showChatWindow(Player playerInstance, int val)
	{
		if (playerInstance == null)
			return;
		
		
		if (FOSEvent.isParticipating())
		{
			final boolean isParticipant = FOSEvent.isPlayerParticipant(playerInstance.getObjectId());
			final String htmContent;
			
			if (!isParticipant)
				htmContent = HtmCache.getInstance().getHtm(foshtmlPath + "Participation.htm");
			else
				htmContent = HtmCache.getInstance().getHtm(foshtmlPath + "RemoveParticipation.htm");
			
			if (htmContent != null)
			{
				int[] teamsPlayerCounts = FOSEvent.getTeamsPlayerCounts();
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
				
				npcHtmlMessage.setHtml(htmContent);
				npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
				npcHtmlMessage.replace("%team1name%", FOSConfig.FOS_EVENT_TEAM_1_NAME);
				npcHtmlMessage.replace("%team1playercount%", String.valueOf(teamsPlayerCounts[0]));
				npcHtmlMessage.replace("%team2name%", FOSConfig.FOS_EVENT_TEAM_2_NAME);
				npcHtmlMessage.replace("%team2playercount%", String.valueOf(teamsPlayerCounts[1]));
				npcHtmlMessage.replace("%playercount%", String.valueOf(teamsPlayerCounts[0] + teamsPlayerCounts[1]));
				playerInstance.sendPacket(npcHtmlMessage);
			}
		}
		else if (FOSEvent.isStarting() || FOSEvent.isStarted())
		{
			final String htmContent = HtmCache.getInstance().getHtm(foshtmlPath + "Status.htm");
			
			if (htmContent != null)
			{
				int[] teamsPlayerCounts = FOSEvent.getTeamsPlayerCounts();
				int[] teamsPointsCounts = FOSEvent.getTeamsPoints();
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
				
				npcHtmlMessage.setHtml(htmContent);
				npcHtmlMessage.replace("%team1name%", FOSConfig.FOS_EVENT_TEAM_1_NAME);
				npcHtmlMessage.replace("%team1playercount%", String.valueOf(teamsPlayerCounts[0]));
				npcHtmlMessage.replace("%team1points%", String.valueOf(teamsPointsCounts[0]));
				npcHtmlMessage.replace("%team2name%", FOSConfig.FOS_EVENT_TEAM_2_NAME);
				npcHtmlMessage.replace("%team2playercount%", String.valueOf(teamsPlayerCounts[1]));
				npcHtmlMessage.replace("%team2points%", String.valueOf(teamsPointsCounts[1])); // <---- array index from 0 to 1 thx DaRkRaGe
				playerInstance.sendPacket(npcHtmlMessage);
			}
		}
		playerInstance.sendPacket(ActionFailed.STATIC_PACKET);
	}
}