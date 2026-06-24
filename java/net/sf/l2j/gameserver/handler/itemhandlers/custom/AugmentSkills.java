package net.sf.l2j.gameserver.handler.itemhandlers.custom;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class AugmentSkills implements IItemHandler
{
//	@Override
//	public void useItem(L2Playable playable, ItemInstance item, boolean forceUse)
//	{
//		if (!(playable instanceof L2PcInstance))
//			return;
//		
//		L2PcInstance activeChar = (L2PcInstance) playable;
//		
//		if (activeChar.isOlympiadProtection() || activeChar.isInCombat() || activeChar.isInOlympiadMode() || activeChar.isDead())
//		{
//			activeChar.sendMessage("This item cannot be used on Olympiad Games.");
//			return;
//		}
//		
//		int itemId = item.getItemId();
//		
//		if (itemId == Config.SKILL_BUFF_ARGUMENT_1) // Item Skill: Might Lv.10
//			activeChar.useMagic(SkillTable.getInstance().getInfo(Config.SKILL_BUFF_ARGUMENT_1_ID, Config.SKILL_BUFF_ARGUMENT_1_LEVEL), false, false);
//		
//		if (itemId == Config.SKILL_BUFF_ARGUMENT_2) // Item Skill: Shield Lv.10
//			activeChar.useMagic(SkillTable.getInstance().getInfo(Config.SKILL_BUFF_ARGUMENT_2_ID, Config.SKILL_BUFF_ARGUMENT_2_LEVEL), false, false);
//		
//		if (itemId == Config.SKILL_BUFF_ARGUMENT_3) // Item Skill: Duel Might Lv.10
//			activeChar.useMagic(SkillTable.getInstance().getInfo(Config.SKILL_BUFF_ARGUMENT_3_ID, Config.SKILL_BUFF_ARGUMENT_3_LEVEL), false, false);
//		
//		if (itemId == Config.SKILL_BUFF_ARGUMENT_4) // Item Skill: Empower Lv.10
//			activeChar.useMagic(SkillTable.getInstance().getInfo(Config.SKILL_BUFF_ARGUMENT_4_ID, Config.SKILL_BUFF_ARGUMENT_4_LEVEL), false, false);
//		
//		if (itemId == Config.SKILL_BUFF_ARGUMENT_5) // Item Skill: Magic Barrier Lv.10
//			activeChar.useMagic(SkillTable.getInstance().getInfo(Config.SKILL_BUFF_ARGUMENT_5_ID, Config.SKILL_BUFF_ARGUMENT_5_LEVEL), false, false);
//		
//		if (itemId == Config.SKILL_BUFF_ARGUMENT_6) // Item Skill: Wild Magic Lv.10
//			activeChar.useMagic(SkillTable.getInstance().getInfo(Config.SKILL_BUFF_ARGUMENT_6_ID, Config.SKILL_BUFF_ARGUMENT_6_LEVEL), false, false);
//		
//		if (itemId == Config.SKILL_BUFF_ARGUMENT_7) // Item Skill: Blessed Body Lv.10
//			activeChar.useMagic(SkillTable.getInstance().getInfo(Config.SKILL_BUFF_ARGUMENT_7_ID, Config.SKILL_BUFF_ARGUMENT_7_LEVEL), false, false);
//		
//		if (itemId == Config.SKILL_BUFF_ARGUMENT_8) // Item Skill: Heal Empower Lv.10
//			activeChar.useMagic(SkillTable.getInstance().getInfo(Config.SKILL_BUFF_ARGUMENT_8_ID, Config.SKILL_BUFF_ARGUMENT_8_LEVEL), false, false);
//		
//		if (itemId == Config.SKILL_BUFF_ARGUMENT_9) // Item Skill: Agility Lv.10
//			activeChar.useMagic(SkillTable.getInstance().getInfo(Config.SKILL_BUFF_ARGUMENT_9_ID, Config.SKILL_BUFF_ARGUMENT_9_LEVEL), false, false);
//		
//		if (itemId == Config.SKILL_BUFF_ARGUMENT_10) // Item Skill: Guidance Lv.10
//			activeChar.useMagic(SkillTable.getInstance().getInfo(Config.SKILL_BUFF_ARGUMENT_10_ID, Config.SKILL_BUFF_ARGUMENT_10_LEVEL), false, false);
//		
//		if (itemId == Config.SKILL_BUFF_ARGUMENT_11) // Item Skill: Focus Lv.10
//			activeChar.useMagic(SkillTable.getInstance().getInfo(Config.SKILL_BUFF_ARGUMENT_11_ID, Config.SKILL_BUFF_ARGUMENT_11_LEVEL), false, false);
//		
//		if (itemId == Config.SKILL_BUFF_ARGUMENT_12) // Item Skill: Refresh Lv.10
//			activeChar.useMagic(SkillTable.getInstance().getInfo(Config.SKILL_BUFF_ARGUMENT_12_ID, Config.SKILL_BUFF_ARGUMENT_12_LEVEL), false, false);
//	}
		private static final Map<Integer, AbstractMap.SimpleEntry<Integer, Integer>> SKILL_BUFF_MAP = new HashMap<>();
			
		static {
			SKILL_BUFF_MAP.put(Config.SKILL_BUFF_ARGUMENT_1, new AbstractMap.SimpleEntry<>(Config.SKILL_BUFF_ARGUMENT_1_ID, Config.SKILL_BUFF_ARGUMENT_1_LEVEL));
			SKILL_BUFF_MAP.put(Config.SKILL_BUFF_ARGUMENT_2, new AbstractMap.SimpleEntry<>(Config.SKILL_BUFF_ARGUMENT_2_ID, Config.SKILL_BUFF_ARGUMENT_2_LEVEL));
			SKILL_BUFF_MAP.put(Config.SKILL_BUFF_ARGUMENT_3, new AbstractMap.SimpleEntry<>(Config.SKILL_BUFF_ARGUMENT_3_ID, Config.SKILL_BUFF_ARGUMENT_3_LEVEL));
			SKILL_BUFF_MAP.put(Config.SKILL_BUFF_ARGUMENT_4, new AbstractMap.SimpleEntry<>(Config.SKILL_BUFF_ARGUMENT_4_ID, Config.SKILL_BUFF_ARGUMENT_4_LEVEL));
			SKILL_BUFF_MAP.put(Config.SKILL_BUFF_ARGUMENT_5, new AbstractMap.SimpleEntry<>(Config.SKILL_BUFF_ARGUMENT_5_ID, Config.SKILL_BUFF_ARGUMENT_5_LEVEL));
			SKILL_BUFF_MAP.put(Config.SKILL_BUFF_ARGUMENT_6, new AbstractMap.SimpleEntry<>(Config.SKILL_BUFF_ARGUMENT_6_ID, Config.SKILL_BUFF_ARGUMENT_6_LEVEL));
			SKILL_BUFF_MAP.put(Config.SKILL_BUFF_ARGUMENT_7, new AbstractMap.SimpleEntry<>(Config.SKILL_BUFF_ARGUMENT_7_ID, Config.SKILL_BUFF_ARGUMENT_7_LEVEL));
			SKILL_BUFF_MAP.put(Config.SKILL_BUFF_ARGUMENT_8, new AbstractMap.SimpleEntry<>(Config.SKILL_BUFF_ARGUMENT_8_ID, Config.SKILL_BUFF_ARGUMENT_8_LEVEL));
			SKILL_BUFF_MAP.put(Config.SKILL_BUFF_ARGUMENT_9, new AbstractMap.SimpleEntry<>(Config.SKILL_BUFF_ARGUMENT_9_ID, Config.SKILL_BUFF_ARGUMENT_9_LEVEL));
			SKILL_BUFF_MAP.put(Config.SKILL_BUFF_ARGUMENT_10, new AbstractMap.SimpleEntry<>(Config.SKILL_BUFF_ARGUMENT_10_ID, Config.SKILL_BUFF_ARGUMENT_10_LEVEL));
			SKILL_BUFF_MAP.put(Config.SKILL_BUFF_ARGUMENT_11, new AbstractMap.SimpleEntry<>(Config.SKILL_BUFF_ARGUMENT_11_ID, Config.SKILL_BUFF_ARGUMENT_11_LEVEL));
			SKILL_BUFF_MAP.put(Config.SKILL_BUFF_ARGUMENT_12, new AbstractMap.SimpleEntry<>(Config.SKILL_BUFF_ARGUMENT_12_ID, Config.SKILL_BUFF_ARGUMENT_12_LEVEL));
		}
		@Override
		public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
			if (!(playable instanceof Player)) {
				return;
			}

			Player player = (Player) playable;

			if (player.isOlympiadProtection() || player.isInCombat() || player.isInOlympiadMode() || player.isDead()) {
				player.sendMessage("This item cannot be used in Olympiad Games or while in combat.");
				return;
			}

			AbstractMap.SimpleEntry<Integer, Integer> skillEntry = SKILL_BUFF_MAP.get(item.getItemId());
			if (skillEntry != null) {
				int skillId = skillEntry.getKey();
				int skillLevel = skillEntry.getValue();
				L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);

				if (skill != null) {
					player.useMagic(skill, false, false);
				}
			}
		}	
}