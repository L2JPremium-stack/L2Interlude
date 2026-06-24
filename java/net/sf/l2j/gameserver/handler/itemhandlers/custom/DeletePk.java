package net.sf.l2j.gameserver.handler.itemhandlers.custom;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;

public class DeletePk implements IItemHandler
{
    @Override
    public void useItem(final Playable playable, final ItemInstance item, final boolean forceUse)
    {
        if (!(playable instanceof Player))
            return;

        final Player activeChar = (Player) playable;

        if (activeChar.isAllSkillsDisabled())
        {
            activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        if (activeChar.isOlympiadProtection() || activeChar.isInCombat() || activeChar.isInOlympiadMode() || activeChar.isDead())
        {
            activeChar.sendMessage("This item cannot be used during combat, Olympiad or while dead.");
            return; // ← Adicionado
        }

        if (activeChar.getPkKills() == 0)
        {
            activeChar.sendMessage("You do not have any PKs to remove.");
            return; // ← Adicionado
        }

        // Remoção de PKs
        activeChar.setPkKills(0);
        playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
        activeChar.sendMessage("Your PKs have been removed.");
        activeChar.sendPacket(new UserInfo(activeChar));
    }
}
