package net.sf.l2j.gameserver.handler.itemhandlers.custom;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.instancemanager.custom.HeroManagerCustom;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

public class HeroItemClick implements IItemHandler
{
	protected static final Logger LOGGER = Logger.getLogger(HeroItemClick.class.getName());
	private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*(d|dias|h|horas)", Pattern.CASE_INSENSITIVE);

	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;

		Player activeChar = (Player) playable;

		if (activeChar.isOlympiadProtection() || activeChar.isInCombat() || activeChar.isInOlympiadMode() || activeChar.isDead())
		{
			activeChar.sendMessage("SYS: Cannot use item while Dead, in Combat or Olympiad.");
			return;
		}

		if (activeChar.isHero())
		{
			activeChar.sendMessage("SYS: You already have Hero status.");
			return;
		}

		String name = item.getItem().getName();
		long durationMs = 0;

		try
		{
			Matcher matcher = DURATION_PATTERN.matcher(name);
			if (matcher.find())
			{
				int value = Integer.parseInt(matcher.group(1));
				String unit = matcher.group(2).toLowerCase();

				if (unit.startsWith("d"))
					durationMs = value * 24L * 60 * 60 * 1000;
				else if (unit.startsWith("h"))
					durationMs = value * 60L * 60 * 1000;
			}
		}
		catch (Exception e)
		{
			activeChar.sendMessage("SYS: Failed to read Hero duration from item name.");
			return;
		}

		if (durationMs <= 0)
		{
			activeChar.sendMessage("SYS: Invalid Hero duration.");
			return;
		}

		if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
		{
			activeChar.sendMessage("SYS: Failed to consume Hero item.");
			return;
		}

		final long now = System.currentTimeMillis();
		long currentEnd = Math.max(now, HeroManagerCustom.getInstance().getHeroDuration(activeChar.getObjectId()));
		long newEndTime = currentEnd + durationMs;

		if (HeroManagerCustom.getInstance().hasHeroPrivileges(activeChar.getObjectId()))
			HeroManagerCustom.getInstance().updateHero(activeChar.getObjectId(), newEndTime);
		else
			HeroManagerCustom.getInstance().addHero(activeChar.getObjectId(), newEndTime);

		String formatted = new SimpleDateFormat("dd MMM, HH:mm").format(new Date(newEndTime));
		activeChar.sendPacket(new ExShowScreenMessage("Your Hero status expires in " + formatted + ".", 10000));
		activeChar.sendMessage("SYS: Your Hero status expires in " + formatted + ".");
	}
}
