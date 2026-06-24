package net.sf.l2j.gameserver.handler.itemhandlers.custom;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;

public class VipItemClick implements IItemHandler
{
	private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*(d|dias|h|horas)", Pattern.CASE_INSENSITIVE);

	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;

		Player activeChar = (Player) playable;

		if (activeChar.isOlympiadProtection() || activeChar.isInCombat() || activeChar.isInOlympiadMode() || activeChar.isDead() || activeChar.isVip())
		{
			activeChar.sendMessage("SYS: Cannot use item now.");
			return;
		}

		String name = item.getItem().getName();
		Matcher matcher = DURATION_PATTERN.matcher(name);
		long durationMs = 0;

		if (matcher.find())
		{
			int value = Integer.parseInt(matcher.group(1));
			String unit = matcher.group(2).toLowerCase();

			if (unit.startsWith("d"))
				durationMs = value * 24L * 60 * 60 * 1000;
			else if (unit.startsWith("h"))
				durationMs = value * 60L * 60 * 1000;
		}
		else
		{
			activeChar.sendMessage("SYS: Invalid VIP duration format.");
			return;
		}

		if (durationMs <= 0)
		{
			activeChar.sendMessage("SYS: VIP duration is invalid.");
			return;
		}

		if (activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false))
		{
			long now = Calendar.getInstance().getTimeInMillis();
			long currentEnd = Math.max(now, activeChar.getVipEndTime());
			long newEnd = currentEnd + durationMs;

			activeChar.setVip(true);
			activeChar.setVipEndTime(newEnd);

			String type = (durationMs >= 86400000L) ? "day(s)" : "hour(s)";
			long amount = (type.equals("day(s)")) ? (durationMs / 86400000L) : (durationMs / 3600000L);

			activeChar.sendMessage("SYS: You became VIP for " + amount + " " + type + ".");

			if (Config.ALLOW_VIP_NCOLOR)
				activeChar.getAppearance().setNameColor(Config.VIP_NCOLOR);

			if (Config.ALLOW_VIP_TCOLOR)
				activeChar.getAppearance().setTitleColor(Config.VIP_TCOLOR);

			activeChar.broadcastUserInfo();
			activeChar.sendPacket(new EtcStatusUpdate(activeChar));
		}
	}
}
