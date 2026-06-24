package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.event.bossevent.KTBEvent;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class L2BossEventStartInstance extends L2NpcInstance
{
	private static final String ktbhtmlPath = "data/html/mods/BossEvent/";

	public L2BossEventStartInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player playerInstance, String command)
	{
		KTBEvent.onBypass(command, playerInstance);
	}

	@Override
	public void showChatWindow(Player playerInstance, int val)
	{
		if (playerInstance == null)
			return;

		 if (KTBEvent.isParticipating())
		{
			final boolean isParticipant = KTBEvent.isPlayerParticipant(playerInstance.getObjectId()); 
			final String htmContent;

			if (!isParticipant)
				htmContent = HtmCache.getInstance().getHtm(ktbhtmlPath + "Participation.htm");
			else
				htmContent = HtmCache.getInstance().getHtm(ktbhtmlPath + "RemoveParticipation.htm");

			if (htmContent != null)
			{
				int PlayerCounts = KTBEvent.getPlayerCounts();
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

				npcHtmlMessage.setHtml(htmContent);
				npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
				npcHtmlMessage.replace("%playercount%", String.valueOf(PlayerCounts));
				if (!isParticipant)
					npcHtmlMessage.replace("%fee%", KTBEvent.getParticipationFee());

				playerInstance.sendPacket(npcHtmlMessage);
			}
		}

		playerInstance.sendPacket(ActionFailed.STATIC_PACKET);
	}
}