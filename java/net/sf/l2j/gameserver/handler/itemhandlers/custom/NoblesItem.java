package net.sf.l2j.gameserver.handler.itemhandlers.custom;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

public class NoblesItem implements IItemHandler
{
    private static final int SCREEN_MSG_TIME = 6000;
    private static final int SCREEN_MSG_TYPE = 0x02; // tipo da mensagem (verde)
    
    @Override
    public void useItem(Playable playable, ItemInstance item, boolean forceUse)
    {
        if (!(playable instanceof Player))
            return;
        
        Player activeChar = (Player) playable;
        
        if (activeChar.isOlympiadProtection() || activeChar.isInCombat() || activeChar.isInOlympiadMode() || activeChar.isDead())
        {
            activeChar.sendMessage("This item cannot be used during Olympiad or combat.");
            return;
        }
        
        if (activeChar.isNoble())
        {
        	activeChar.sendMessage("[Noble System]: You already have the Noble status.");
            return;
        }
        
        // Tenta consumir o item antes de aplicar status
        if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
        {
            activeChar.sendMessage("Failed to consume the item.");
            return;
        }
        
        // Aplica o status Nobre e atualiza os dados
        activeChar.setNoble(true, true);
        activeChar.broadcastUserInfo();
        activeChar.sendPacket(new ExShowScreenMessage("Congratulations! You have become Noble.", SCREEN_MSG_TIME, SCREEN_MSG_TYPE, true));

    }
}
