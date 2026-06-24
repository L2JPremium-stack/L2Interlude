package net.sf.l2j.gameserver.handler.itemhandlers.custom;

import java.util.List;

import net.sf.l2j.event.partyfarm.RewardHolder;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

public class BoxRewards implements IItemHandler
{
	private static final String EMPTY_MESSAGE = "This box has no rewards configured.";
	private static final String RESTRICTED_MESSAGE = "You cannot use this item right now.";

	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;

		final Player player = (Player) playable;

		// Verifica restrições
		if (isUsageRestricted(player))
		{
			player.sendMessage(RESTRICTED_MESSAGE);
			return;
		}

		// Pega lista de recompensas fixas
		final List<RewardHolder> rewardPool = getAllRewards(item.getItem());

		if (rewardPool == null || rewardPool.isEmpty())
		{
			player.sendMessage(EMPTY_MESSAGE);
		}
		else
		{
			// Dá todos os itens configurados
			for (RewardHolder reward : rewardPool)
			{
				int amount = reward.getRewardMin(); // min=max=amount no parser
				player.addItem("BoxRewards", reward.getRewardId(), amount, player, true);
			}
		}

		// Consome a caixinha
		player.destroyItem("BoxRewardsUse", item.getObjectId(), 1, null, false);
		player.broadcastPacket(new MagicSkillUse(player, player, 2024, 1, 1, 0));
	}

	private static boolean isUsageRestricted(Player player)
	{
		return player.isDead()
			|| player.isInCombat()
			|| player.isOlympiadProtection()
			|| player.isInOlympiadMode();
	}

	// Busca todos os rewards fixos do XML
	private static List<RewardHolder> getAllRewards(Item itemTemplate)
	{
		return itemTemplate.getBoxRewards(); // precisa implementar no parser de Item
	}
}
