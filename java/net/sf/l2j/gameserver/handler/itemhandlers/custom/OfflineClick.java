package net.sf.l2j.gameserver.handler.itemhandlers.custom;

import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

public class OfflineClick implements IItemHandler
{
	private static final int COUNTDOWN_SECONDS = 5;
	private static final int MESSAGE_DISPLAY_TIME = 950;

	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;

		final Player activeChar = (Player) playable;

		if ((!activeChar.isInStoreMode() && !activeChar.isCrafting()) || !activeChar.isSitting())
		{
			activeChar.sendMessage("You are not running a private store or private workshop.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isInCombat())
		{
			activeChar.sendMessage("You cannot logout while in combat mode.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isFestivalParticipant())
		{
			activeChar.sendMessage("You can't use this item while participating in the Festival!");
			return;
		}

		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("You can't teleport while you are in Olympiad.");
			return;
		}

		if (activeChar.inObserverMode())
		{
			activeChar.sendMessage("You can't teleport while you are in observer mode.");
			return;
		}

		activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
		initCountdown(activeChar, COUNTDOWN_SECONDS);

		ThreadPool.schedule(() -> activeChar.logout(true), COUNTDOWN_SECONDS * 1000L);
	}

	private class Countdown implements Runnable
	{
		private final Player player;
		private int timeLeft;

		public Countdown(Player player, int timeLeft)
		{
			this.player = player;
			this.timeLeft = timeLeft;
		}

		@Override
		public void run()
		{
			if (!player.isOnline())
				return;

			player.sendPacket(new ExShowScreenMessage(
				"Disconnecting in " + timeLeft + " second" + (timeLeft > 1 ? "s" : "") + "!", 
				MESSAGE_DISPLAY_TIME));

			if (timeLeft > 1)
			{
				ThreadPool.schedule(new Countdown(player, timeLeft - 1), 1000L);
			}
		}
	}

	private void initCountdown(Player activeChar, int duration)
	{
		if (activeChar != null && activeChar.isOnline())
		{
			ThreadPool.schedule(new Countdown(activeChar, duration), 0L);
		}
	}
}
