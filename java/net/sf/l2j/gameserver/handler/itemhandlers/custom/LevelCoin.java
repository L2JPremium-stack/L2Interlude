package net.sf.l2j.gameserver.handler.itemhandlers.custom;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

public class LevelCoin implements IItemHandler
{
    private static final int TARGET_LEVEL = 80;
    private static final long TARGET_EXP = Experience.LEVEL[TARGET_LEVEL + 1]; // LEVEL array index 81 = level 80 exp threshold
    
    @Override
    public void useItem(Playable playable, ItemInstance item, boolean forceUse)
    {
        if (!(playable instanceof Player))
            return;
        
        final Player activeChar = (Player) playable;
        
        // Verifica condições para uso do item
        if (activeChar.isOlympiadProtection() || activeChar.isInCombat() || activeChar.isInOlympiadMode() || activeChar.isDead())
        {
            activeChar.sendMessage("You cannot use this item right now.");
            return;
        }
        
        int currentLevel = activeChar.getLevel();
        long currentExp = activeChar.getExp();
        
        // Verifica se já está no nível 80 ou acima
        if (currentLevel >= TARGET_LEVEL)
        {
            activeChar.sendMessage("You are already level 80 or higher. This item cannot be used.");
            return;
        }
        
        // Calcula experiência necessária para alcançar o nível 80
        long expToAdd = TARGET_EXP - currentExp;
        if (expToAdd <= 0)
        {
            activeChar.sendMessage("You have enough experience to be level 80.");
            return;
        }
        
        // Tenta destruir o item para consumir
        if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
        {
            activeChar.sendMessage("Failed to consume the item.");
            return;
        }
        
        // Adiciona experiência para chegar ao nível 80
        activeChar.addExpAndSp(expToAdd, 0);
        
        // Envia mensagem de parabéns na tela
        activeChar.sendPacket(new ExShowScreenMessage("Congratulations! You have reached level 80.", 6000, ExShowScreenMessage.SMPOS.TOP_CENTER, true));
    }
}
