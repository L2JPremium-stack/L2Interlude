package net.sf.l2j.gameserver.handler.itemhandlers.custom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;

public class AioItemClick implements IItemHandler
{
    @Override
    public void useItem(Playable playable, ItemInstance item, boolean forceUse)
    {
        if (!(playable instanceof Player))
            return;

        Player activeChar = (Player) playable;

        if (activeChar.isOlympiadProtection() || activeChar.isInCombat() || activeChar.isInOlympiadMode() || activeChar.isDead() || activeChar.isAio())
        {
			activeChar.sendMessage("SYS: Cannot use item now.");
            return;
        }

        // EXTRAI duração e unidade do nome, ex: "AIO Pass 7d" ou "AIO Pass 2h"
        long durationMs = 0;
        try
        {
            String name = item.getItem().getName().toLowerCase(); // "aio pass 7d", "aio pass 2h"
            Pattern pattern = Pattern.compile("(\\d+)\\s*(d|dias|h|horas?)");
            Matcher matcher = pattern.matcher(name);

            if (matcher.find())
            {
                int value = Integer.parseInt(matcher.group(1));
                String unit = matcher.group(2);

                if (unit.startsWith("d"))
                    durationMs = value * 86400000L;  // dias para ms
                else if (unit.startsWith("h"))
                    durationMs = value * 3600000L;   // horas para ms
            }
        }
        catch (Exception e)
        {
            activeChar.sendMessage("SYS: Failed to read duration from item name.");
            return;
        }

        if (durationMs <= 0)
        {
            activeChar.sendMessage("SYS: Invalid AIO duration.");
            return;
        }

        if (activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false))
        {
            long now = System.currentTimeMillis();
            long currentEnd = activeChar.isAio() ? Math.max(now, activeChar.getAioEndTime()) : now;
            long newEnd = currentEnd + durationMs;

            activeChar.setAio(true);
            activeChar.setAioEndTime(newEnd);

            // Calcula dias e horas restantes para a mensagem
            long diff = newEnd - now;
            long days = diff / 86400000L;
            long hours = (diff % 86400000L) / 3600000L;

            activeChar.sendMessage("SYS: AIO activated for " + (days > 0 ? days + "d " : "") + (hours > 0 ? hours + "h" : "") + ".");

            if (Config.ALLOW_AIO_NCOLOR)
                activeChar.getAppearance().setNameColor(Config.AIO_NCOLOR);

            if (Config.ALLOW_AIO_TCOLOR)
                activeChar.getAppearance().setTitleColor(Config.AIO_TCOLOR);

            if (Config.ALLOW_AIO_ITEM)
            {
                activeChar.getInventory().addItem("", Config.AIO_ITEMID, 1, activeChar, null);
                activeChar.getInventory().equipItem(activeChar.getInventory().getItemByItemId(Config.AIO_ITEMID));
            }

            activeChar.getStat().addExp(activeChar.getStat().getExpForLevel(81));
            activeChar.rewardAioSkills();
            activeChar.sendSkillList();
            activeChar.broadcastUserInfo();
            activeChar.sendPacket(new EtcStatusUpdate(activeChar));
        }
    }
}
