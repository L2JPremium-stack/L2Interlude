package net.sf.l2j.gameserver.handler.itemhandlers.custom;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class AugmentStone implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		Player player = (Player) playable;
		
		ItemInstance weapon = player.getActiveWeaponInstance();
		
		if (weapon == null)
		{
			player.sendMessage("You need equip a weapon first.");
			return;
		}
		
		if (player.isInCombat() || player.isDead())
		{
			player.sendMessage("Cannot use this item now.");
			return;
		}
		
		player.setTempAugmentItem(item.getObjectId());
		
		NpcHtmlMessage html = new NpcHtmlMessage(0);

		StringBuilder sb = new StringBuilder();
		sb.append("<html><body><center>");

		sb.append("<font color=\"LEVEL\">AUGMENT SYSTEM</font><br>");
		sb.append("<img src=\"L2UI.SquareGray\" width=260 height=1><br><br>");

		sb.append("Welcome to the Augment System.<br1>");
		sb.append("Choose an option below:<br><br>");

		// ===== OPTIONS =====
		sb.append("<button value=\"Passive\" ");
		sb.append("action=\"bypass voiced_augment passive\" ");
		sb.append("width=95 height=25 back=\"L2UI_JDEV.AdminGreenBtn_Down\" fore=\"L2UI_JDEV.AdminGreenBtn\"><br>");

		sb.append("<button value=\"Active\" ");
		sb.append("action=\"bypass voiced_augment active\" ");
		sb.append("width=95 height=25 back=\"L2UI_JDEV.AdminGreenBtn_Down\" fore=\"L2UI_JDEV.AdminGreenBtn\"><br><br>");

		sb.append("<button value=\"How it Works\" ");
		sb.append("action=\"bypass voiced_augment info\" ");
		sb.append("width=95 height=25 back=\"L2UI_JDEV.AdminGreenBtn_Down\" fore=\"L2UI_JDEV.AdminGreenBtn\"><br>");

		// ===== GM ONLY =====
		if (player.isGM())
		{
		    sb.append("<br><font color=\"FF9900\">[GM PANEL]</font><br>");
		    
		    sb.append("<button value=\"Debug\" ");
		    sb.append("action=\"bypass voiced_augment admin\" ");
		    sb.append("width=95 height=25 back=\"L2UI_JDEV.AdminGreenBtn_Down\" fore=\"L2UI_JDEV.AdminGreenBtn\">");
		}

		sb.append("</center></body></html>");

		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	
}