package net.sf.l2j.gameserver.handler.itemhandlers.custom;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.event.partyfarm.RewardHolder;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

public class LuckBox implements IItemHandler {

	private static final String EMPTY_MESSAGE = "Oh no! Your lucky box is empty.";
	private static final String RESTRICTED_MESSAGE = "You cannot use this item right now.";

	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
		if (!(playable instanceof Player))
			return;

		final Player player = (Player) playable;

		if (isUsageRestricted(player)) {
			player.sendMessage(RESTRICTED_MESSAGE);
			return;
		}

		final List<RewardHolder> rewardPool = getValidRewards(item.getItem());

		if (rewardPool.isEmpty()) {
			player.sendMessage(EMPTY_MESSAGE);
		} else {
			final RewardHolder selected = rewardPool.get(Rnd.get(rewardPool.size()));
			final int count = Rnd.get(selected.getRewardMin(), selected.getRewardMax());
			player.addItem("LuckBox", selected.getRewardId(), count, player, true);
		}

		player.destroyItem("LuckBoxUse", item.getObjectId(), 1, null, false);
		player.broadcastPacket(new MagicSkillUse(player, player, 2024, 1, 1, 0));
	}

	private static boolean isUsageRestricted(Player player) {
		return player.isDead()
			|| player.isInCombat()
			|| player.isOlympiadProtection()
			|| player.isInOlympiadMode();
	}

	private static List<RewardHolder> getValidRewards(Item itemTemplate) {
		final List<RewardHolder> result = new ArrayList<>();

		for (RewardHolder reward : itemTemplate.getLuckBoxRewards()) {
			if (Rnd.get(100) < reward.getRewardChance()) {
				result.add(reward);
			}
		}

		return result;
	}
}
