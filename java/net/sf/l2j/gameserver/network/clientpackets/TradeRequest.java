package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SendTradeRequest;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Util;

public final class TradeRequest extends L2GameClientPacket
{
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		final Player target = L2World.getInstance().getPlayer(_objectId);
		if (target == null || !player.getKnownList().knowsObject(target) || target.equals(player))
		{
			player.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}
		
		if (target.isInOlympiadMode() || player.isInOlympiadMode())
		{
			player.sendMessage("You or your target cannot trade during Olympiad.");
			return;
		}
		// Alt game - Karma punishment
		if (!Config.KARMA_PLAYER_CAN_TRADE && (player.getKarma() > 0 || target.getKarma() > 0))
		{
			player.sendMessage("You cannot trade in a chaotic state.");
			return;
		}
		
		if (player.isInStoreMode() || target.isInStoreMode())
		{
			player.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}
		if (target.isOff())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addPcName(target));
			return;
		}
		if (player.isProcessingTransaction())
		{
			player.sendPacket(SystemMessageId.ALREADY_TRADING);
			return;
		}
		
		if (target.isProcessingRequest() || target.isProcessingTransaction())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addPcName(target);
			player.sendPacket(sm);
			return;
		}
		
		if (target.getTradeRefusal())
		{
			player.sendMessage("Your target is in trade refusal mode.");
			return;
		}
		
		if (BlockList.isBlocked(target, player))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST).addPcName(target);
			player.sendPacket(sm);
			return;
		}
		
		if (Util.calculateDistance(player, target, true) > L2Npc.INTERACTION_DISTANCE)
		{
			player.sendPacket(SystemMessageId.TARGET_TOO_FAR);
			return;
		}
		
		player.onTransactionRequest(target);
		target.sendPacket(new SendTradeRequest(player.getObjectId()));
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REQUEST_S1_FOR_TRADE).addPcName(target));
	}
}