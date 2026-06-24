package net.sf.l2j.gameserver.handler.itemhandlers.custom;

//public class GradeDReward implements IItemHandler
//{
//	@Override
//	public void useItem(L2Playable playable, ItemInstance item, boolean forceUse)
//	{
//		if (!(playable instanceof L2PcInstance))
//			return;
//
//		L2PcInstance activeChar = (L2PcInstance)playable;
//
//		if (activeChar.isOlympiadProtection() || activeChar.isInCombat() || activeChar.isInOlympiadMode() || activeChar.isDead())
//		{
//			activeChar.sendMessage("This item cannot be used on Olympiad Games.");
//			return;
//		}
//
//		if (activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false))
//		{
//			//Heavy
//			if (activeChar.getClassId() == ClassId.WARRIOR)
//			{
//				//Armor
//				ItemInstance item1 = activeChar.getInventory().addItem("Armor", 352, 1, activeChar, null);
//				ItemInstance item2 = activeChar.getInventory().addItem("Armor", 2378, 1, activeChar, null);
//				ItemInstance item3 = activeChar.getInventory().addItem("Armor", 2411, 1, activeChar, null);
//				ItemInstance item4 = activeChar.getInventory().addItem("Armor", 2425, 1, activeChar, null);
//				ItemInstance item5 = activeChar.getInventory().addItem("Armor", 2449, 1, activeChar, null);
//				
//				//Weapon
//				ItemInstance item6 = activeChar.getInventory().addItem("Weapon", 2525, 1, activeChar, null);
//				
//				//Jewels
//				ItemInstance item7 = activeChar.getInventory().addItem("Ring", 881, 1, activeChar, null);
//				ItemInstance item8 = activeChar.getInventory().addItem("Ring", 881, 1, activeChar, null);
//				ItemInstance item9 = activeChar.getInventory().addItem("Earring", 850, 1, activeChar, null);
//				ItemInstance item10 = activeChar.getInventory().addItem("Earring", 850, 1, activeChar, null);
//				ItemInstance item11 = activeChar.getInventory().addItem("Necklace", 913, 1, activeChar, null);
//				
//				//Equipp
//				activeChar.getInventory().equipItemAndRecord(item1);
//				activeChar.getInventory().equipItemAndRecord(item2);
//				activeChar.getInventory().equipItemAndRecord(item3);
//				activeChar.getInventory().equipItemAndRecord(item4);
//				activeChar.getInventory().equipItemAndRecord(item5);
//				activeChar.getInventory().equipItemAndRecord(item6);
//				activeChar.getInventory().equipItemAndRecord(item7);
//				activeChar.getInventory().equipItemAndRecord(item8);
//				activeChar.getInventory().equipItemAndRecord(item9);
//				activeChar.getInventory().equipItemAndRecord(item10);
//				activeChar.getInventory().equipItemAndRecord(item11);
//				
//				//Secund Weapon
//				activeChar.getInventory().addItem("Weapon", 297, 1, activeChar, null);
//				
//				//Misc
//				activeChar.getInventory().addItem("Soul Shot Grade D", 1463, 2000, activeChar, null);
//				activeChar.getInventory().addItem("Mana Potion", 728, 100, activeChar, null);
//				activeChar.getInventory().addItem("Greater Healing Potion", 1539, 20, activeChar, null);
//				activeChar.getInventory().addItem("Scroll of Scape", 736, 5, activeChar, null);
//				
//				activeChar.getInventory().updateDatabase();
//				activeChar.sendPacket(new ItemList(activeChar, true));
//
//				playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
//				MagicSkillUse MSU = new MagicSkillUse(activeChar, activeChar, 2024, 1, 1, 0);
//				activeChar.broadcastPacket(MSU);
//			}
//			//Heavy
//			if (activeChar.getClassId() == ClassId.KNIGHT || activeChar.getClassId() == ClassId.ELVEN_KNIGHT || activeChar.getClassId() == ClassId.PALUS_KNIGHT || activeChar.getClassId() == ClassId.SCAVENGER || activeChar.getClassId() == ClassId.ARTISAN)
//			{
//				//Armor
//				ItemInstance item1 = activeChar.getInventory().addItem("Armor", 352, 1, activeChar, null);
//				ItemInstance item2 = activeChar.getInventory().addItem("Armor", 2378, 1, activeChar, null);
//				ItemInstance item3 = activeChar.getInventory().addItem("Armor", 2411, 1, activeChar, null);
//				ItemInstance item4 = activeChar.getInventory().addItem("Armor", 2425, 1, activeChar, null);
//				ItemInstance item5 = activeChar.getInventory().addItem("Armor", 2449, 1, activeChar, null);
//				
//				//Weapon
//				ItemInstance item6 = activeChar.getInventory().addItem("Weapon", 2499, 1, activeChar, null);
//				
//				//Jewels
//				ItemInstance item7 = activeChar.getInventory().addItem("Ring", 881, 1, activeChar, null);
//				ItemInstance item8 = activeChar.getInventory().addItem("Ring", 881, 1, activeChar, null);
//				ItemInstance item9 = activeChar.getInventory().addItem("Earring", 850, 1, activeChar, null);
//				ItemInstance item10 = activeChar.getInventory().addItem("Earring", 850, 1, activeChar, null);
//				ItemInstance item11 = activeChar.getInventory().addItem("Necklace", 913, 1, activeChar, null);
//				
//				//Shield
//				ItemInstance item12 = activeChar.getInventory().addItem("Shield", 2493, 1, activeChar, null);
//				
//				//Equipp
//				activeChar.getInventory().equipItemAndRecord(item1);
//				activeChar.getInventory().equipItemAndRecord(item2);
//				activeChar.getInventory().equipItemAndRecord(item3);
//				activeChar.getInventory().equipItemAndRecord(item4);
//				activeChar.getInventory().equipItemAndRecord(item5);
//				activeChar.getInventory().equipItemAndRecord(item6);
//				activeChar.getInventory().equipItemAndRecord(item7);
//				activeChar.getInventory().equipItemAndRecord(item8);
//				activeChar.getInventory().equipItemAndRecord(item9);
//				activeChar.getInventory().equipItemAndRecord(item10);
//				activeChar.getInventory().equipItemAndRecord(item11);
//				activeChar.getInventory().equipItemAndRecord(item12);
//				
//				//Secund Weapon
//				activeChar.getInventory().addItem("Weapon", 159, 1, activeChar, null);
//				
//				//Misc
//				activeChar.getInventory().addItem("Soul Shot Grade D", 1463, 2000, activeChar, null);
//				activeChar.getInventory().addItem("Mana Potion", 728, 100, activeChar, null);
//				activeChar.getInventory().addItem("Greater Healing Potion", 1539, 20, activeChar, null);
//				activeChar.getInventory().addItem("Scroll of Scape", 736, 5, activeChar, null);
//				
//				activeChar.getInventory().updateDatabase();
//				activeChar.sendPacket(new ItemList(activeChar, true));
//
//				playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
//				MagicSkillUse MSU = new MagicSkillUse(activeChar, activeChar, 2024, 1, 1, 0);
//				activeChar.broadcastPacket(MSU);
//			}
//			//Light
//			if (activeChar.getClassId() == ClassId.ROGUE || activeChar.getClassId() == ClassId.ELVEN_SCOUT || activeChar.getClassId() == ClassId.ASSASSIN)
//			{
//				//Armor
//				ItemInstance item1 = activeChar.getInventory().addItem("Armor", 395, 1, activeChar, null);
//				ItemInstance item2 = activeChar.getInventory().addItem("Armor", 417, 1, activeChar, null);
//				ItemInstance item3 = activeChar.getInventory().addItem("Armor", 2424, 1, activeChar, null);
//				ItemInstance item4 = activeChar.getInventory().addItem("Armor", 2448, 1, activeChar, null);
//				ItemInstance item5 = activeChar.getInventory().addItem("Armor", 2412, 1, activeChar, null);
//				
//				//Weapon
//				ItemInstance item6 = activeChar.getInventory().addItem("Weapon", 225, 1, activeChar, null);
//				
//				//Jewels
//				ItemInstance item7 = activeChar.getInventory().addItem("Ring", 881, 1, activeChar, null);
//				ItemInstance item8 = activeChar.getInventory().addItem("Ring", 881, 1, activeChar, null);
//				ItemInstance item9 = activeChar.getInventory().addItem("Earring", 850, 1, activeChar, null);
//				ItemInstance item10 = activeChar.getInventory().addItem("Earring", 850, 1, activeChar, null);
//				ItemInstance item11 = activeChar.getInventory().addItem("Necklace", 913, 1, activeChar, null);
//				
//				//Equipp
//				activeChar.getInventory().equipItemAndRecord(item1);
//				activeChar.getInventory().equipItemAndRecord(item2);
//				activeChar.getInventory().equipItemAndRecord(item3);
//				activeChar.getInventory().equipItemAndRecord(item4);
//				activeChar.getInventory().equipItemAndRecord(item5);
//				activeChar.getInventory().equipItemAndRecord(item6);
//				activeChar.getInventory().equipItemAndRecord(item7);
//				activeChar.getInventory().equipItemAndRecord(item8);
//				activeChar.getInventory().equipItemAndRecord(item9);
//				activeChar.getInventory().equipItemAndRecord(item10);
//				activeChar.getInventory().equipItemAndRecord(item11);
//				
//				//Secund Weapon
//				activeChar.getInventory().addItem("Weapon", 280, 1, activeChar, null);
//				
//				//Misc
//				activeChar.getInventory().addItem("Soul Shot Grade D", 1463, 2000, activeChar, null);
//				activeChar.getInventory().addItem("Arrow D", 1341, 2000, activeChar, null);
//				activeChar.getInventory().addItem("Mana Potion", 728, 100, activeChar, null);
//				activeChar.getInventory().addItem("Greater Healing Potion", 1539, 20, activeChar, null);
//				activeChar.getInventory().addItem("Scroll of Scape", 736, 5, activeChar, null);
//				
//				activeChar.getInventory().updateDatabase();
//				activeChar.sendPacket(new ItemList(activeChar, true));
//
//				playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
//				MagicSkillUse MSU = new MagicSkillUse(activeChar, activeChar, 2024, 1, 1, 0);
//				activeChar.broadcastPacket(MSU);
//			}
//			//Robe
//			if (activeChar.getClassId() == ClassId.HUMAN_WIZARD || activeChar.getClassId() == ClassId.CLERIC || activeChar.getClassId() == ClassId.ELVEN_WIZARD || activeChar.getClassId() == ClassId.ELVEN_ORACLE || activeChar.getClassId() == ClassId.DARK_WIZARD || activeChar.getClassId() == ClassId.SHILLIEN_ORACLE || activeChar.getClassId() == ClassId.ORC_SHAMAN)
//			{
//				//Armor
//				ItemInstance item1 = activeChar.getInventory().addItem("Armor", 437, 1, activeChar, null);
//				ItemInstance item2 = activeChar.getInventory().addItem("Armor", 470, 1, activeChar, null);
//				ItemInstance item3 = activeChar.getInventory().addItem("Armor", 2450, 1, activeChar, null);
//				ItemInstance item4 = activeChar.getInventory().addItem("Armor", 2426, 1, activeChar, null);
//				ItemInstance item5 = activeChar.getInventory().addItem("Armor", 2412, 1, activeChar, null);
//				
//				//Weapon
//				ItemInstance item6 = activeChar.getInventory().addItem("Weapon", 189, 1, activeChar, null);
//				
//				//Jewels
//				ItemInstance item7 = activeChar.getInventory().addItem("Ring", 881, 1, activeChar, null);
//				ItemInstance item8 = activeChar.getInventory().addItem("Ring", 881, 1, activeChar, null);
//				ItemInstance item9 = activeChar.getInventory().addItem("Earring", 850, 1, activeChar, null);
//				ItemInstance item10 = activeChar.getInventory().addItem("Earring", 850, 1, activeChar, null);
//				ItemInstance item11 = activeChar.getInventory().addItem("Necklace", 913, 1, activeChar, null);
//				
//				//Shield
//				ItemInstance item12 = activeChar.getInventory().addItem("Shield", 629, 1, activeChar, null);
//				
//				//Equipp
//				activeChar.getInventory().equipItemAndRecord(item1);
//				activeChar.getInventory().equipItemAndRecord(item2);
//				activeChar.getInventory().equipItemAndRecord(item3);
//				activeChar.getInventory().equipItemAndRecord(item4);
//				activeChar.getInventory().equipItemAndRecord(item5);
//				activeChar.getInventory().equipItemAndRecord(item6);
//				activeChar.getInventory().equipItemAndRecord(item7);
//				activeChar.getInventory().equipItemAndRecord(item8);
//				activeChar.getInventory().equipItemAndRecord(item9);
//				activeChar.getInventory().equipItemAndRecord(item10);
//				activeChar.getInventory().equipItemAndRecord(item11);
//				activeChar.getInventory().equipItemAndRecord(item12);
//
//				//Misc
//				activeChar.getInventory().addItem("Spirit Shot Grade D", 3948, 2000, activeChar, null);
//				activeChar.getInventory().addItem("Mana Potion", 728, 100, activeChar, null);
//				activeChar.getInventory().addItem("Greater Healing Potion", 1539, 20, activeChar, null);
//				activeChar.getInventory().addItem("Scroll of Scape", 736, 5, activeChar, null);
//				
//				activeChar.getInventory().updateDatabase();
//				activeChar.sendPacket(new ItemList(activeChar, true));
//
//				playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
//				MagicSkillUse MSU = new MagicSkillUse(activeChar, activeChar, 2024, 1, 1, 0);
//				activeChar.broadcastPacket(MSU);
//			}
//			//Heavy
//			if (activeChar.getClassId() == ClassId.ORC_RAIDER)
//			{
//				//Armor
//				ItemInstance item1 = activeChar.getInventory().addItem("Armor", 352, 1, activeChar, null);
//				ItemInstance item2 = activeChar.getInventory().addItem("Armor", 2378, 1, activeChar, null);
//				ItemInstance item3 = activeChar.getInventory().addItem("Armor", 2411, 1, activeChar, null);
//				ItemInstance item4 = activeChar.getInventory().addItem("Armor", 2425, 1, activeChar, null);
//				ItemInstance item5 = activeChar.getInventory().addItem("Armor", 2449, 1, activeChar, null);
//				
//				//Weapon
//				ItemInstance item6 = activeChar.getInventory().addItem("Weapon", 70, 1, activeChar, null);
//				
//				//Jewels
//				ItemInstance item7 = activeChar.getInventory().addItem("Ring", 881, 1, activeChar, null);
//				ItemInstance item8 = activeChar.getInventory().addItem("Ring", 881, 1, activeChar, null);
//				ItemInstance item9 = activeChar.getInventory().addItem("Earring", 850, 1, activeChar, null);
//				ItemInstance item10 = activeChar.getInventory().addItem("Earring", 850, 1, activeChar, null);
//				ItemInstance item11 = activeChar.getInventory().addItem("Necklace", 913, 1, activeChar, null);
//				
//				//Equipp
//				activeChar.getInventory().equipItemAndRecord(item1);
//				activeChar.getInventory().equipItemAndRecord(item2);
//				activeChar.getInventory().equipItemAndRecord(item3);
//				activeChar.getInventory().equipItemAndRecord(item4);
//				activeChar.getInventory().equipItemAndRecord(item5);
//				activeChar.getInventory().equipItemAndRecord(item6);
//				activeChar.getInventory().equipItemAndRecord(item7);
//				activeChar.getInventory().equipItemAndRecord(item8);
//				activeChar.getInventory().equipItemAndRecord(item9);
//				activeChar.getInventory().equipItemAndRecord(item10);
//				activeChar.getInventory().equipItemAndRecord(item11);
//				
//				//Secund Weapon
//				activeChar.getInventory().addItem("Weapon", 297, 1, activeChar, null);
//				
//				//Misc
//				activeChar.getInventory().addItem("Soul Shot Grade D", 1463, 2000, activeChar, null);
//				activeChar.getInventory().addItem("Mana Potion", 728, 100, activeChar, null);
//				activeChar.getInventory().addItem("Greater Healing Potion", 1539, 20, activeChar, null);
//				activeChar.getInventory().addItem("Scroll of Scape", 736, 5, activeChar, null);
//				
//				activeChar.getInventory().updateDatabase();
//				activeChar.sendPacket(new ItemList(activeChar, true));
//
//				playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
//				MagicSkillUse MSU = new MagicSkillUse(activeChar, activeChar, 2024, 1, 1, 0);
//				activeChar.broadcastPacket(MSU);
//			}
//			//Light
//			if (activeChar.getClassId() == ClassId.MONK)
//			{
//				//Armor
//				ItemInstance item1 = activeChar.getInventory().addItem("Armor", 395, 1, activeChar, null);
//				ItemInstance item2 = activeChar.getInventory().addItem("Armor", 417, 1, activeChar, null);
//				ItemInstance item3 = activeChar.getInventory().addItem("Armor", 2424, 1, activeChar, null);
//				ItemInstance item4 = activeChar.getInventory().addItem("Armor", 2448, 1, activeChar, null);
//				ItemInstance item5 = activeChar.getInventory().addItem("Armor", 2412, 1, activeChar, null);
//				
//				//Weapon
//				ItemInstance item6 = activeChar.getInventory().addItem("Weapon", 262, 1, activeChar, null);
//				
//				//Jewels
//				ItemInstance item7 = activeChar.getInventory().addItem("Ring", 881, 1, activeChar, null);
//				ItemInstance item8 = activeChar.getInventory().addItem("Ring", 881, 1, activeChar, null);
//				ItemInstance item9 = activeChar.getInventory().addItem("Earring", 850, 1, activeChar, null);
//				ItemInstance item10 = activeChar.getInventory().addItem("Earring", 850, 1, activeChar, null);
//				ItemInstance item11 = activeChar.getInventory().addItem("Necklace", 913, 1, activeChar, null);
//				
//				//Equipp
//				activeChar.getInventory().equipItemAndRecord(item1);
//				activeChar.getInventory().equipItemAndRecord(item2);
//				activeChar.getInventory().equipItemAndRecord(item3);
//				activeChar.getInventory().equipItemAndRecord(item4);
//				activeChar.getInventory().equipItemAndRecord(item5);
//				activeChar.getInventory().equipItemAndRecord(item6);
//				activeChar.getInventory().equipItemAndRecord(item7);
//				activeChar.getInventory().equipItemAndRecord(item8);
//				activeChar.getInventory().equipItemAndRecord(item9);
//				activeChar.getInventory().equipItemAndRecord(item10);
//				activeChar.getInventory().equipItemAndRecord(item11);
//				
//				//Misc
//				activeChar.getInventory().addItem("Soul Shot Grade D", 1463, 2000, activeChar, null);
//				activeChar.getInventory().addItem("Arrow D", 1341, 2000, activeChar, null);
//				activeChar.getInventory().addItem("Mana Potion", 728, 100, activeChar, null);
//				activeChar.getInventory().addItem("Greater Healing Potion", 1539, 20, activeChar, null);
//				activeChar.getInventory().addItem("Scroll of Scape", 736, 5, activeChar, null);
//				
//				activeChar.getInventory().updateDatabase();
//				activeChar.sendPacket(new ItemList(activeChar, true));
//
//				playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
//				MagicSkillUse MSU = new MagicSkillUse(activeChar, activeChar, 2024, 1, 1, 0);
//				activeChar.broadcastPacket(MSU);
//			}
//		}
//	}
import java.util.EnumSet;
import java.util.List;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

public class GradeDReward implements IItemHandler
{
	
	// Define os grupos de classes por tipo de armadura
	private static final EnumSet<ClassId> HEAVY_CLASSES = EnumSet.of(ClassId.WARRIOR, ClassId.KNIGHT, ClassId.ELVEN_KNIGHT, ClassId.PALUS_KNIGHT, ClassId.SCAVENGER, ClassId.ARTISAN, ClassId.ORC_RAIDER);
	private static final EnumSet<ClassId> LIGHT_CLASSES = EnumSet.of(ClassId.ROGUE, ClassId.ELVEN_SCOUT, ClassId.ASSASSIN, ClassId.MONK);
	private static final EnumSet<ClassId> ROBE_CLASSES = EnumSet.of(ClassId.HUMAN_WIZARD, ClassId.CLERIC, ClassId.ELVEN_WIZARD, ClassId.ELVEN_ORACLE, ClassId.DARK_WIZARD, ClassId.SHILLIEN_ORACLE, ClassId.ORC_SHAMAN);
	
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		Player player = (Player) playable;
		
		if (player.isOlympiadProtection() || player.isInCombat() || player.isInOlympiadMode() || player.isDead())
		{
			player.sendMessage("This item cannot be used in Olympiad Games or while in combat.");
			return;
		}
		
		ClassId classId = player.getClassId();
		
		if (!HEAVY_CLASSES.contains(classId) && !LIGHT_CLASSES.contains(classId) && !ROBE_CLASSES.contains(classId))
		{
			player.sendMessage("Your class is not eligible to use this item.");
			return; // ← evita consumo indevido
		}
		
		// Agora sim, consome o item
		if (!player.destroyItem("Consume", item.getObjectId(), 1, null, false))
			return;
		
		// Dá a recompensa conforme a classe
		if (HEAVY_CLASSES.contains(classId))
		{
			giveHeavyReward(player, classId);
		}
		else if (LIGHT_CLASSES.contains(classId))
		{
			giveLightReward(player, classId);
		}
		else if (ROBE_CLASSES.contains(classId))
		{
			giveRobeReward(player);
		}
		
		player.getInventory().updateDatabase();
		player.sendPacket(new ItemList(player, true));
		player.broadcastPacket(new MagicSkillUse(player, player, 2024, 1, 1, 0));
	}
	
	private static void equipAndAdd(List<Integer> itemIds, Player player)
	{
		for (int itemId : itemIds)
		{
			ItemInstance item = player.getInventory().addItem("Reward", itemId, 1, player, null);
			player.getInventory().equipItemAndRecord(item);
		}
	}
	
	private static void giveHeavyReward(Player player, ClassId classId)
	{
		// Equipamentos comuns para Heavy
		equipAndAdd(List.of(352, 2378, 2411, 2425, 2449), player); // Armor
		equipAndAdd(List.of(881, 881, 850, 850, 913), player); // Jewels
		
		int weaponId = 0;
		
		switch (classId)
		{
			
			case WARRIOR:
				weaponId = 2525;
				break;
			
			case KNIGHT:
			case ELVEN_KNIGHT:
			case PALUS_KNIGHT:
			case SCAVENGER:
			case ARTISAN:
				weaponId = 2499;
				break;
			
			case ORC_RAIDER:
				weaponId = 70;
				break;
			
		}
		
		if (weaponId != 0)
		{
			ItemInstance weapon = player.getInventory().addItem("Weapon", weaponId, 1, player, null);
			player.getInventory().equipItemAndRecord(weapon);
		}
		
		// Shields
		if (EnumSet.of(ClassId.KNIGHT, ClassId.ELVEN_KNIGHT, ClassId.PALUS_KNIGHT, ClassId.SCAVENGER, ClassId.ARTISAN).contains(classId))
		{
			ItemInstance shield = player.getInventory().addItem("Shield", 2493, 1, player, null);
			player.getInventory().equipItemAndRecord(shield);
		}
		
		int secondWeaponId;
		
		switch (classId)
		{
			case WARRIOR:
			case ORC_RAIDER:
				secondWeaponId = 297;
				break;
			
			case KNIGHT:
			case ELVEN_KNIGHT:
			case PALUS_KNIGHT:
			case SCAVENGER:
			case ARTISAN:
				secondWeaponId = 159;
				break;
			
			default:
				secondWeaponId = 0;
				break;
		}
		
		if (secondWeaponId != 0)
			player.getInventory().addItem("Weapon", secondWeaponId, 1, player, null);
		
		// Itens diversos
		player.getInventory().addItem("Soul Shot Grade D", 1463, 2000, player, null);
		player.getInventory().addItem("Mana Potion", 728, 100, player, null);
		player.getInventory().addItem("Greater Healing Potion", 1539, 20, player, null);
		player.getInventory().addItem("Scroll of Escape", 736, 5, player, null);
	}
	
	private static void giveLightReward(Player player, ClassId classId)
	{
		equipAndAdd(List.of(395, 417, 2424, 2448, 2412), player); // Armor
		equipAndAdd(List.of(881, 881, 850, 850, 913), player); // Jewels
		
		int weaponId;
		
		switch (classId)
		{
			case ROGUE:
			case ELVEN_SCOUT:
			case ASSASSIN:
				weaponId = 225;
				break;
			
			case MONK:
				
				weaponId = 262;
				break;
			
			default:
				weaponId = 0;
				break;
		}
		
		if (weaponId != 0)
		{
			ItemInstance weapon = player.getInventory().addItem("Weapon", weaponId, 1, player, null);
			player.getInventory().equipItemAndRecord(weapon);
		}
		
		// Segunda arma (somente para classes que usam arco)
		if (EnumSet.of(ClassId.ROGUE, ClassId.ELVEN_SCOUT, ClassId.ASSASSIN).contains(classId))
			player.getInventory().addItem("Weapon", 280, 1, player, null);
		
		// Itens diversos
		player.getInventory().addItem("Soul Shot Grade D", 1463, 2000, player, null);
		player.getInventory().addItem("Arrow D", 1341, 2000, player, null);
		player.getInventory().addItem("Mana Potion", 728, 100, player, null);
		player.getInventory().addItem("Greater Healing Potion", 1539, 20, player, null);
		player.getInventory().addItem("Scroll of Escape", 736, 5, player, null);
	}
	
	private static void giveRobeReward(Player player)
	{
		equipAndAdd(List.of(437, 470, 2450, 2426, 2412), player); // Armor
		equipAndAdd(List.of(881, 881, 850, 850, 913), player); // Jewels
		
		// Arma
		ItemInstance weapon = player.getInventory().addItem("Weapon", 189, 1, player, null);
		player.getInventory().equipItemAndRecord(weapon);
		
		// Escudo
		ItemInstance shield = player.getInventory().addItem("Shield", 629, 1, player, null);
		player.getInventory().equipItemAndRecord(shield);
		
		// Itens diversos
		player.getInventory().addItem("Spirit Shot Grade D", 3948, 2000, player, null);
		player.getInventory().addItem("Mana Potion", 728, 100, player, null);
		player.getInventory().addItem("Greater Healing Potion", 1539, 20, player, null);
		player.getInventory().addItem("Scroll of Escape", 736, 5, player, null);
	}
}