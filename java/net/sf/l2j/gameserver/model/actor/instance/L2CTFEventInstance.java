package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.event.ctf.CTFConfig;
import net.sf.l2j.event.ctf.CTFEvent;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class L2CTFEventInstance extends L2NpcInstance
{
	private static final String ctfhtmlPath = "data/html/mods/events/ctf/";
	
	public L2CTFEventInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player playerInstance, String command)
	{
		CTFEvent.onBypass(command, playerInstance);
	}
	
	@Override
	public void showChatWindow(Player playerInstance, int val)
	{
		if (playerInstance == null)
			return;
		
		 if (CTFEvent.isParticipating())
		{
			final boolean isParticipant = CTFEvent.isPlayerParticipant(playerInstance.getObjectId());
			final String htmContent;
			
			if (!isParticipant)
				htmContent = HtmCache.getInstance().getHtm(ctfhtmlPath + "Participation.htm");
			else
				htmContent = HtmCache.getInstance().getHtm(ctfhtmlPath + "RemoveParticipation.htm");
			
			if (htmContent != null)
			{
				int[] teamsPlayerCounts = CTFEvent.getTeamsPlayerCounts();
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
				
				npcHtmlMessage.setHtml(htmContent);
				npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
				npcHtmlMessage.replace("%team1name%", CTFConfig.CTF_EVENT_TEAM_1_NAME);
				npcHtmlMessage.replace("%team1playercount%", String.valueOf(teamsPlayerCounts[0]));
				npcHtmlMessage.replace("%team2name%", CTFConfig.CTF_EVENT_TEAM_2_NAME);
				npcHtmlMessage.replace("%team2playercount%", String.valueOf(teamsPlayerCounts[1]));
				npcHtmlMessage.replace("%playercount%", String.valueOf(teamsPlayerCounts[0] + teamsPlayerCounts[1]));
				playerInstance.sendPacket(npcHtmlMessage);
			}
		}
		else if (CTFEvent.isStarting() || CTFEvent.isStarted())
		{
			final String htmContent = HtmCache.getInstance().getHtm(ctfhtmlPath + "Status.htm");
			
			if (htmContent != null)
			{
				int[] teamsPlayerCounts = CTFEvent.getTeamsPlayerCounts();
				int[] teamsPointsCounts = CTFEvent.getTeamsPoints();
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
				
				npcHtmlMessage.setHtml(htmContent);
				npcHtmlMessage.replace("%team1name%", CTFConfig.CTF_EVENT_TEAM_1_NAME);
				npcHtmlMessage.replace("%team1playercount%", String.valueOf(teamsPlayerCounts[0]));
				npcHtmlMessage.replace("%team1points%", String.valueOf(teamsPointsCounts[0]));
				npcHtmlMessage.replace("%team2name%", CTFConfig.CTF_EVENT_TEAM_2_NAME);
				npcHtmlMessage.replace("%team2playercount%", String.valueOf(teamsPlayerCounts[1]));
				npcHtmlMessage.replace("%team2points%", String.valueOf(teamsPointsCounts[1])); // <---- array index from 0 to 1 thx DaRkRaGe
				playerInstance.sendPacket(npcHtmlMessage);
			}
		}
		playerInstance.sendPacket(ActionFailed.STATIC_PACKET);
	}
}