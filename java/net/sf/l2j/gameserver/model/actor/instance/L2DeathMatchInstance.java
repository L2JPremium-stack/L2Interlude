package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.event.deathmath.DMConfig;
import net.sf.l2j.event.deathmath.DMEvent;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class L2DeathMatchInstance extends L2NpcInstance
{
	private static final String dmhtmlPath = "data/html/mods/events/dm/";
	
	public L2DeathMatchInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player playerInstance, String command)
	{
		DMEvent.onBypass(command, playerInstance);
	}
	
	@Override
	public void showChatWindow(Player playerInstance, int val)
	{
		if (playerInstance == null)
			return;
		
		if (DMEvent.isParticipating())
		{
			final boolean isParticipant = DMEvent.isPlayerParticipant(playerInstance.getObjectId()); 
			final String htmContent;

			if (!isParticipant)
			    htmContent = HtmCache.getInstance().getHtm(dmhtmlPath + "Participation.htm");
			else
				htmContent = HtmCache.getInstance().getHtm(dmhtmlPath + "RemoveParticipation.htm");

	    	if (htmContent != null)
	    	{
	    		int PlayerCounts = DMEvent.getPlayerCounts();
	    		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

				npcHtmlMessage.setHtml(htmContent);
	    		npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
				npcHtmlMessage.replace("%playercount%", String.valueOf(PlayerCounts));
				if (!isParticipant)
					npcHtmlMessage.replace("%fee%", DMEvent.getParticipationFee());

				playerInstance.sendPacket(npcHtmlMessage);
	    	}
		}
		else if (DMEvent.isStarting() || DMEvent.isStarted())
		{
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			final String htmContent = HtmCache.getInstance().getHtm(dmhtmlPath + "Status.htm");

			String[] topPositions;
			String htmltext = "";
			if (DMConfig.DM_SHOW_TOP_RANK)
			{
				topPositions = DMEvent.getFirstPosition(DMConfig.DM_TOP_RANK);
				Boolean c = true;
				String c1 = "LEVEL";
				String c2 = "FFFFFF";
				if (topPositions != null)
					for (int i = 0; i < topPositions.length; i++)
					{
						String color = (c ? c1 : c2);
						String[] row = topPositions[i].split("\\,");
						htmltext += "<tr>";
						htmltext += "<td width=\"35\" align=\"center\"><font color=\"" + color + "\">" + String.valueOf(i + 1) + "</font></td>";
						htmltext += "<td width=\"100\" align=\"left\"><font color=\"" + color + "\">" + row[0] + "</font></td>";
						htmltext += "<td width=\"125\" align=\"right\"><font color=\"" + color + "\">" + row[1] + "</font></td>";
						htmltext += "</tr>";
						c = !c;
					}
			}
			
	    	if (htmContent != null)
	    	{
				npcHtmlMessage.setHtml(htmContent);
				npcHtmlMessage.replace("%positions%", htmltext);
				playerInstance.sendPacket(npcHtmlMessage);
	    	}
		}
		
		playerInstance.sendPacket(ActionFailed.STATIC_PACKET);
	}
}