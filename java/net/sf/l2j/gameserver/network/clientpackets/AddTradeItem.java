package net.sf.l2j.gameserver.network.clientpackets;

import java.util.Arrays;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.custom.AdminItemLockManager;
import net.sf.l2j.gameserver.instancemanager.custom.CustomAugmentManager;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.tradelist.TradeItem;
import net.sf.l2j.gameserver.model.tradelist.TradeList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.TradeItemUpdate;
import net.sf.l2j.gameserver.network.serverpackets.TradeOtherAdd;
import net.sf.l2j.gameserver.network.serverpackets.TradeOwnAdd;

public final class AddTradeItem extends L2GameClientPacket
{
	private int _tradeId;
	private int _objectId;
	private int _count;
	
	public AddTradeItem()
	{
	}
	
	@Override
	protected void readImpl()
	{
		_tradeId = readD();
		_objectId = readD();
		_count = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		final TradeList trade = player.getActiveTradeList();
		if (trade == null)
		{
			_log.warning("Character: " + player.getName() + " requested item:" + _objectId + " add without active tradelist:" + _tradeId);
			return;
		}
		
		final Player partner = trade.getPartner();
		if (partner == null || L2World.getInstance().getPlayer(partner.getObjectId()) == null || partner.getActiveTradeList() == null)
		{
			// Trade partner not found, cancel trade
			if (partner != null)
				_log.warning(player.getName() + " requested invalid trade object: " + _objectId);
			
			player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			player.cancelActiveTrade();
			return;
		}
		
		if (trade.isConfirmed() || partner.getActiveTradeList().isConfirmed())
		{
			player.sendPacket(SystemMessageId.CANNOT_ADJUST_ITEMS_AFTER_TRADE_CONFIRMED);
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			player.cancelActiveTrade();
			return;
		}
		
		if (!player.validateItemManipulation(_objectId))
		{
			player.sendPacket(SystemMessageId.NOTHING_HAPPENED);
			return;
		}
		
		final ItemInstance itemInstance = player.getInventory().getItemByObjectId(_objectId);
		if (!AdminItemLockManager.getInstance().check(player, itemInstance))
		{
			player.sendPacket(SystemMessageId.NOTHING_HAPPENED);
			return;
		}
		
		final TradeItem item = trade.addItem(_objectId, _count);
		if (item != null)
		{
			final ItemInstance Status = player.getInventory().getItemByObjectId(_objectId);
			if (Status.isAugmented() && Config.AUG_ITEM_TRADE)
			{
				final L2Augmentation augment = Status.getAugmentation();
				final L2Skill augSkill = augment.getSkill();
				if (augSkill != null && augSkill.isPassive())
				{
					player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_ALL, "", "   {Trade} " + Status.getName() + " Enchanted + " + Status.getEnchantLevel() + " Argument " + augSkill.getName() + " Passive"));
					partner.sendPacket(new CreatureSay(0, Say2.PARTYROOM_ALL, "", "   {Trade} " + Status.getName() + " Enchanted + " + Status.getEnchantLevel() + " Argument " + augSkill.getName() + " Passive"));
				}
				// partner.sendMessage(String.format("%s - %s ", Status.getName(), "Passive " + augSkill.getName()));
				else if (augSkill != null && augSkill.isActive())
				{
					player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_ALL, "", "   {Trade} " + Status.getName() + " Enchanted + " + Status.getEnchantLevel() + " Argument " + augSkill.getName() + " Active"));
					partner.sendPacket(new CreatureSay(0, Say2.PARTYROOM_ALL, "", "   {Trade} " + Status.getName() + " Enchanted + " + Status.getEnchantLevel() + " Argument " + augSkill.getName() + " Active"));
				}
				// partner.sendMessage(String.format("%s - %s ", Status.getName(), "Active " + augSkill.getName()));
				else if (augSkill != null && augSkill.isChance())
				{
					partner.sendMessage(String.format("%s - %s ", Status.getName(), "Chance " + augSkill.getName()));
				}
				else
					partner.sendMessage(String.format("%s - No augment skill.", Status.getName()));
				
				partner.sendMessage("======= Stats ========");
				Arrays.asList(augment.getDetails()).forEach(partner::sendMessage);
			}
			
			int[] data = CustomAugmentManager.getInstance().getAugment(Status.getObjectId());
			
			if (data != null)
			{
				int skillId = data[0];
				int level = data[1];
				
				L2Skill skill = SkillTable.getInstance().getInfo(skillId, level);
				
				if (skill != null)
				{
					String type;
					
					if (skill.isPassive())
						type = "Passive";
					else if (skill.isActive())
						type = "Active";
					else if (skill.isChance())
						type = "Chance";
					else
						type = "Unknown";
					
					String msg = "   {Trade} " + Status.getName() + " Enchanted + " + Status.getEnchantLevel() + " AugmentStone " + skill.getName() + " " + type;
					
					player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_ALL, "", msg));
					partner.sendPacket(new CreatureSay(0, Say2.PARTYROOM_ALL, "", msg));
				}
			}
			
			player.sendPacket(new TradeOwnAdd(item));
			player.sendPacket(new TradeItemUpdate(trade, player));
			trade.getPartner().sendPacket(new TradeOtherAdd(item));
		}

	}
}