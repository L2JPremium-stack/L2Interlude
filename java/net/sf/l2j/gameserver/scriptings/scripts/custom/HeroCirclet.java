package net.sf.l2j.gameserver.scriptings.scripts.custom;

import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scriptings.Quest;
import net.sf.l2j.gameserver.scriptings.QuestState;

public class HeroCirclet extends Quest
{
	public HeroCirclet()
	{
		super(-1, "custom");
		
		addStartNpc(31690, 31769, 31770, 31771, 31772);
		addTalkId(31690, 31769, 31770, 31771, 31772);
	}
	
	@Override
	public String onTalk(L2Npc npc, Player player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
			st = newQuestState(player);
		
		if (player.isHero())
		{
			if (player.getInventory().getItemByItemId(6842) == null)
				st.giveItems(6842, 1);
			else
				htmltext = "already_have_circlet.htm";
		}
		else
			htmltext = "no_hero.htm";
		
		st.exitQuest(true);
		return htmltext;
	}
}
