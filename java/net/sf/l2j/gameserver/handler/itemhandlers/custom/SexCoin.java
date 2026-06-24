package net.sf.l2j.gameserver.handler.itemhandlers.custom;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class SexCoin implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		final Player activeChar = (Player) playable;
		
		if (activeChar.isOlympiadProtection() || activeChar.isInCombat() || activeChar.isInOlympiadMode() || activeChar.isDead())
		{
			activeChar.sendMessage("You can not do that.");
			return;
		}
		// Verifica se o jogador possui um summon ativo (dragão, pet, etc.)
		if (activeChar.hasPet() || activeChar.isMounted() || activeChar.getPet() != null || activeChar.isCastingNow())
		{
			activeChar.sendMessage("You cannot change class while having an active summon or mount.");
			return;
		}
		activeChar.setSexChangeItemId(item.getItemId());
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/CoinCustom/SexChange.htm");
		activeChar.sendPacket(html);
		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
