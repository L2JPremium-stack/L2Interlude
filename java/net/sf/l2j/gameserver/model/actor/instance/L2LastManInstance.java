package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.event.lastman.LMEvent;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class L2LastManInstance extends L2NpcInstance
{
	private static final String lmhtmlPath = "data/html/mods/events/lm/";
	
	public L2LastManInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player playerInstance, String command)
	{
		LMEvent.onBypass(command, playerInstance);
	}
	
	@Override
	public void showChatWindow(Player playerInstance, int val)
	{
		if (playerInstance == null)
			return;
		
		
		if (LMEvent.isParticipating())
		{
			final boolean isParticipant = LMEvent.isPlayerParticipant(playerInstance.getObjectId()); 
			final String htmContent;

			if (!isParticipant)
			    htmContent = HtmCache.getInstance().getHtm(lmhtmlPath + "Participation.htm");
			else
			    htmContent = HtmCache.getInstance().getHtm(lmhtmlPath + "RemoveParticipation.htm");

	    	if (htmContent != null)
	    	{
	    		int PlayerCounts = LMEvent.getPlayerCounts();
	    		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

				npcHtmlMessage.setHtml(htmContent);
	    		npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
				npcHtmlMessage.replace("%playercount%", String.valueOf(PlayerCounts));
				playerInstance.sendPacket(npcHtmlMessage);
	    	}
		}
		else if (LMEvent.isStarting() || LMEvent.isStarted())
		{
			final String htmContent = HtmCache.getInstance().getHtm(lmhtmlPath + "Status.htm");

	    	if (htmContent != null)
	    	{
	    		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
	    		String htmltext = "";
	    		htmltext = String.valueOf(LMEvent.getPlayerCounts());
				npcHtmlMessage.setHtml(htmContent);
				npcHtmlMessage.replace("%countplayer%", htmltext);
				playerInstance.sendPacket(npcHtmlMessage);
	    	}
		}
		playerInstance.sendPacket(ActionFailed.STATIC_PACKET);
	}
}