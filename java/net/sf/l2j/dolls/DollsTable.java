package net.sf.l2j.dolls;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.xmlfactory.XMLDocument;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DollsTable extends XMLDocument
{
    private Map<Integer, DollHolder> runes;

    // Constante para o bônus de drop (aumento de 20% no drop)
    public static final double DROP_BONUS_PERCENTAGE = 2.0;  // 20% de aumento no drop

    public DollsTable()
    {
        runes = new HashMap<>();
        load();
    }

    public void reload() 
    {
        runes.clear();
        load();
    }

    public static DollsTable getInstance() 
    {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder 
    {
        protected static final DollsTable INSTANCE = new DollsTable();
    }

    @Override
    protected void load() 
    {
        loadDocument("./data/xml/custom/Dolls.xml");
        LOGGER.info("DollTable: Loaded " + runes.size() + " dolls.");
    }

    @Override
    protected void parseDocument(Document doc, File file) 
    {
        try 
        {
            final Node root = doc.getFirstChild();

            for (Node node = root.getFirstChild(); node != null; node = node.getNextSibling()) 
            {
                if (!"Doll".equalsIgnoreCase(node.getNodeName()))
                    continue;

                NamedNodeMap attrs = node.getAttributes();
                int id = Integer.parseInt(attrs.getNamedItem("Id").getNodeValue());
                int skillId = Integer.parseInt(attrs.getNamedItem("SkillId").getNodeValue());
                int skillLvl = Integer.parseInt(attrs.getNamedItem("SkillLvl").getNodeValue());
                double skillPower = attrs.getNamedItem("SkillPower") != null 
                    ? Double.parseDouble(attrs.getNamedItem("SkillPower").getNodeValue()) 
                    : 0.0;

                DollHolder rune = new DollHolder(id, skillId, skillLvl, skillPower);
                runes.put(id, rune);
            }
        } 
        catch (Exception e)
        {
            LOGGER.warn("DollsTable: Error while loading doll: " + e.getMessage());
        }
    }

    public Map<Integer, DollHolder> getRunes() 
    {
        return runes;
    }

    public DollHolder getRuneById(int id)
    {
        return runes.get(id);
    }

    public boolean isRuneById(int id)
    {
        return runes.containsKey(id);
    }

    public static DollHolder getRune(Player player)
    {
        int skillLv = 0;
        int itemId = 0;

        for (ItemInstance runeItem : player.getInventory().getItems()) 
        {
            if (DollsTable.getInstance().isRuneById(runeItem.getItemId())) 
            {
                int skillLvl = DollsTable.getInstance().getRuneById(runeItem.getItemId()).getSkillLvl();
                if (skillLvl > skillLv) 
                {
                    skillLv = skillLvl;
                    itemId = runeItem.getItemId();
                }
            }
        }

        if (itemId == 0)
            return null;

        return DollsTable.getInstance().getRuneById(itemId);
    }

    public static void setSkillForRune(Player player, int runeItemId)
    {
        DollHolder rune = DollsTable.getInstance().getRuneById(runeItemId);

        if (rune == null)
            return;

        int skillId = rune.getSkillId();
        int skillLvl = rune.getSkillLvl();

        L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);

        if (skill != null) 
        {
            int currentSkillLvl = player.getSkillLevel(skillId);

            if (currentSkillLvl > 0) 
                player.removeSkill(skillId, true);

            if (player.getInventory().getItemByItemId(runeItemId) == null)
                refreshAllRuneSkills(player);
            else 
                player.addSkill(skill);

            player.sendSkillList();
        }
    }

    public static void refreshAllRuneSkills(Player player) 
    {
        Map<Integer, Integer> highestSkillLevels = new HashMap<>();

        for (ItemInstance runeItem : player.getInventory().getItems()) 
        {
            if (DollsTable.getInstance().isRuneById(runeItem.getItemId()))
            {
                int skillId = DollsTable.getInstance().getRuneById(runeItem.getItemId()).getSkillId();
                int skillLvl = DollsTable.getInstance().getRuneById(runeItem.getItemId()).getSkillLvl();

                if (!highestSkillLevels.containsKey(skillId) || skillLvl > highestSkillLevels.get(skillId))
                    highestSkillLevels.put(skillId, skillLvl);
            }
        }

        for (Map.Entry<Integer, Integer> entry : highestSkillLevels.entrySet()) 
        {
            int skillId = entry.getKey();
            int skillLvl = entry.getValue();

            player.removeSkill(skillId, true);

            if (skillLvl > 0) 
            {
                L2Skill newSkill = SkillTable.getInstance().getInfo(skillId, skillLvl);
                if (newSkill != null) 
                    player.addSkill(newSkill, false);
            }
        }

        player.sendSkillList();
    }

    /*public static void removeRuneSkills(Player player) 
    {
        Map<Integer, Integer> highestSkillLevels = new HashMap<>();

        for (ItemInstance runeItem : player.getInventory().getItems()) 
        {
            if (DollsTable.getInstance().isRuneById(runeItem.getItemId()))
            {
                int skillId = DollsTable.getInstance().getRuneById(runeItem.getItemId()).getSkillId();
                int skillLvl = DollsTable.getInstance().getRuneById(runeItem.getItemId()).getSkillLvl();

                if (!highestSkillLevels.containsKey(skillId) || skillLvl > highestSkillLevels.get(skillId))
                    highestSkillLevels.put(skillId, skillLvl);
            }
        }
        
        for (Map.Entry<Integer, Integer> entry : highestSkillLevels.entrySet()) 
        {
        	L2Skill skill = SkillTable.getInstance().getInfo(entry.getKey(), entry.getValue());
        	if (skill != null)
        	{
        		System.out.print("deleto?");
        		player.removeSkill(skill, false);
        		player.sendSkillList();
        	}
        }
    }*/
    public static void removeRuneSkills(Player player, ItemInstance item) 
    {
        if (item == null || !DollsTable.getInstance().isRuneById(item.getItemId())) 
        {
            return; // Se o item não for um Doll, não faz nada
        }

        // Pega a skill do item específico
        int skillId = DollsTable.getInstance().getRuneById(item.getItemId()).getSkillId();
        int skillLvl = DollsTable.getInstance().getRuneById(item.getItemId()).getSkillLvl();
        
        // Obtém a skill associada ao item (Doll)
        L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);

        if (skill != null) 
        {
            // Remove a skill associada a esse item específico
            player.removeSkill(skill, false);
            player.sendSkillList(); // Atualiza a lista de skills do jogador
        }
    }
    public static void getSkillRune(Player player, ItemInstance item) 
    {
        if (item != null && DollsTable.getInstance().isRuneById(item.getItemId())) 
        {
            setSkillForRune(player, item.getItemId());
            refreshAllRuneSkills(player);
        }
    }

    // Função para aplicar o bônus de drop ao jogador.
    public static void applyDropBonus(Player player) 
    {
        if (player == null)
        	return;

        // Verifica se o jogador está utilizando algum Doll.
        DollHolder highestRune = getRune(player);
        if (highestRune != null) 
        {
            // Se o jogador estiver usando um Doll, concede um bônus no drop.
            double bonus = DROP_BONUS_PERCENTAGE;
            player.setDropBonus(bonus); // Método fictício para aplicar o bônus de drop no jogador.

        //    LOG.info("Doll bonus applied: " + bonus + " for player " + player.getName());
        }
        else 
        {
            // Se não estiver usando um Doll, o bônus é 1 (sem bônus).
            player.setDropBonus(1.0);
        }
    }
}