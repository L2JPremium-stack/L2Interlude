package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class L2SiegeNpcInstance extends L2NpcInstance
{
	public L2SiegeNpcInstance(int objectID, NpcTemplate template)
	{
		super(objectID, template);
	}
	
	@Override
	public void showChatWindow(Player player)
	{
		
		if(Config.DISABLED_CHAT_NPCS_SIEGE)
		{
			player.sendMessage("Npcs Siege is Disabled");
			return;
		}
		
		if (validateCondition())
			getCastle().getSiege().listRegisterClan(player);
		else
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/siege/" + getNpcId() + "-busy.htm");
			html.replace("%castlename%", getCastle().getName());
			html.replace("%objectId%", getObjectId());
			player.sendPacket(html);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	private boolean validateCondition()
	{
		if (getCastle().getSiege().isInProgress())
			return false; // Busy because of siege
			
		return true;
	}
}