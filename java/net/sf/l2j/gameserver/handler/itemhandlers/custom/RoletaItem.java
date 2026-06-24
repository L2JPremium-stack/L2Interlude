package net.sf.l2j.gameserver.handler.itemhandlers.custom;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class RoletaItem implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
	
		int itemId = item.getItemId();
		
		final Player player = (Player) playable;
		
		if(!(playable instanceof Player))
			return;
		
		 if (itemId == Config.CUSTOM_ITEM_ROULETTE)
			showHtm(player, "ROULETTE_MENU");
	}
	
	private static void showHtm(Player player, String type) 
	{
	    NpcHtmlMessage htm = new NpcHtmlMessage(0);
	    htm.setFile("data/html/mods/Roleta/items/" + type + ".htm");
	    player.sendPacket(htm);
	    htm.replace("%ItemNameRoleta%", ItemTable.getInstance().getTemplate(Config.CUSTOM_ITEM_ROULETTE).getName());
	    htm.replace("%ItemCountRoleta%", Config.ITEM_COUNT_ROLETA);
	    
	}
}