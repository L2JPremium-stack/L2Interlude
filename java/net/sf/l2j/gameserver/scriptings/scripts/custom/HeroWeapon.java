package net.sf.l2j.gameserver.scriptings.scripts.custom;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.scriptings.Quest;
import net.sf.l2j.gameserver.scriptings.QuestState;
import net.sf.l2j.gameserver.util.Util;

public class HeroWeapon extends Quest
{
	private static final int[] WEAPON_IDS =
	{
		6611,
		6612,
		6613,
		6614,
		6615,
		6616,
		6617,
		6618,
		6619,
		6620,
		6621
	};
	
	public HeroWeapon()
	{
		super(-1, "custom");
		
		addStartNpc(31690, 31769, 31770, 31771, 31772, 31773);
		addTalkId(31690, 31769, 31770, 31771, 31772, 31773);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, Player player)
	{
	    QuestState st = player.getQuestState(getName());
	    
	    int weaponId = Integer.valueOf(event);
	    if (Util.contains(WEAPON_IDS, weaponId))
	    {
	        // Adiciona o item ao inventário do jogador
	        ItemInstance item = player.getInventory().addItem("give", weaponId, 1, player, npc);
	        
	        if (item != null)
	        {
	            // Se o encantamento for habilitado na configuração
	            if (Config.ENABLE_HERO_WEAPON_ENCHANT)
	            {
	                // Aplica o encantamento fixo da configuração
	                int enchantLevel = Config.HERO_WEAPON_ENCHANT_LEVEL; // Pega o valor de encantamento fixo da configuração
	                item.setEnchantLevel(enchantLevel);
	                
	                // Atualiza o banco de dados para garantir que a modificação seja salva
	           //     player.getInventory().updateDatabase(); // Garante que a arma encantada seja salva no banco de dados
	                
	                // Exibe a mensagem com o nível de encantamento
	                player.sendMessage("You received the weapon " + item.getName() + " enchanted with +" + enchantLevel + "! Thanks and good game!");
	            }
	            else
	            {
	                // Se o encantamento não for habilitado, a mensagem será sem o encantamento
	                player.sendMessage("You received the weapon " + item.getName() + ". Thanks and good game!");
	            }

	            // Atualiza o inventário na interface do jogador
	            player.sendPacket(new ItemList(player, false));
	        }
	    }
	    
	    st.exitQuest(true);
	    return null;
	}


	@Override
	public String onTalk(L2Npc npc, Player player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
			newQuestState(player);
		
		if (st != null)
		{
			if (player.isHero())
			{
				if (hasHeroWeapon(player) && !Config.ENABLE_WEAPONS_HERO_ALL)
				{
					htmltext = "already_have_weapon.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "weapon_list.htm";
			}
			else
			{
				htmltext = "no_hero.htm";
				st.exitQuest(true);
			}
		}
		
		return htmltext;
	}
	
	private static boolean hasHeroWeapon(Player player)
	{
		for (int i : WEAPON_IDS)
		{
			if (player.getInventory().getItemByItemId(i) != null)
				return true;
		}
		
		return false;
	}
}
