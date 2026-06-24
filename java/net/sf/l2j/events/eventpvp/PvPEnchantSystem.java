package net.sf.l2j.events.eventpvp;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.itemcontainer.Inventory;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.util.Broadcast;

public class PvPEnchantSystem 
{
	public static void RewardEnchantChanceByPvp(Player player)
	{
		if (Rnd.get(100) <= Config.PVP_ITEM_ENCHANT_WEAPON_CHANCE)
			RewardEnchantWeaponByPvp(player);
		
		if (Rnd.get(100) <= Config.PVP_ITEM_ENCHANT_ARMOR_CHANCE)
			RewardEnchantArmorByPvp(player);
	}
	
	public static void RewardEnchantWeaponByPvp(Player player)
	{
		final ItemInstance pvpwep = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		
		if (pvpwep == null)
			return;
		
		if (pvpwep.getEnchantLevel() >= Config.CHECK_MAX_ENCHANT_WEAPON)
			return;

		if (pvpwep.getItemId() >= 6611 && pvpwep.getItemId() <= 6621)
			return;
		
		if (!Config.WEAPON_LIST_ID_ENCHANT_RESTRICT.contains(pvpwep.getItemId()))
			return;
		
		if (pvpwep.getEnchantLevel() >= Config.CHECK_MIN_ENCHANT_WEAPON)
		{
			pvpwep.setEnchantLevel(pvpwep.getEnchantLevel() + 1);
			Broadcast.announceEventHideToPlayers(player.getName() + " get an PvP Enchant of " + pvpwep.getItem().getName() + " +" + pvpwep.getEnchantLevel());
			player.sendPacket(new ItemList(player, false));

			MagicSkillUse MSU = new MagicSkillUse(player, player, 2024, 1, 1, 0);
			player.broadcastPacket(MSU);
			player.broadcastUserInfo();
		}
	}
	
	public static void RewardEnchantArmorByPvp(Player player)
	{
		final ItemInstance pvphead = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD);
		final ItemInstance pvpgloves = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
		final ItemInstance pvpchest = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		final ItemInstance pvplegs = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		final ItemInstance pvpfeet = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET);
		final ItemInstance pvpneck = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK);
		final ItemInstance pvplf = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER);
		final ItemInstance pvprf = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER);
		final ItemInstance pvple = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR);
		final ItemInstance pvpra = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR);

		switch (Rnd.get(10))
		{
			case 0:
			{
				if (pvphead == null)
					return;
				
				if (pvphead.getEnchantLevel() >= Config.CHECK_MAX_ENCHANT_ARMOR_JEWELS)
					return;

				if (!Config.ARMOR_LIST_ID_ENCHANT_RESTRICT.contains(pvphead.getItemId()))
					return;
				
				if (pvphead.getEnchantLevel() >= Config.CHECK_MIN_ENCHANT_ARMOR_JEWELS)
				{
					pvphead.setEnchantLevel(pvphead.getEnchantLevel() + 1);
					Broadcast.announceEventHideToPlayers(player.getName() + " get an PvP Enchant of " + pvphead.getItem().getName() + " +" + pvphead.getEnchantLevel());

					player.sendPacket(new ItemList(player, false));

					MagicSkillUse MSU = new MagicSkillUse(player, player, 2024, 1, 1, 0);
					player.broadcastPacket(MSU);

					player.broadcastUserInfo();
				}
				break;
			}
			case 1:
			{
				if (pvpgloves == null)
					return;
				
				if (pvpgloves.getEnchantLevel() >= Config.CHECK_MAX_ENCHANT_ARMOR_JEWELS)
					return;

				if (!Config.ARMOR_LIST_ID_ENCHANT_RESTRICT.contains(pvpgloves.getItemId()))
					return;
				
				if (pvpgloves.getEnchantLevel() >= Config.CHECK_MIN_ENCHANT_ARMOR_JEWELS)
				{
					pvpgloves.setEnchantLevel(pvpgloves.getEnchantLevel() + 1);
					Broadcast.announceEventHideToPlayers(player.getName() + " get an PvP Enchant of " + pvpgloves.getItem().getName() + " +" + pvpgloves.getEnchantLevel());
					player.sendPacket(new ItemList(player, false));

					MagicSkillUse MSU = new MagicSkillUse(player, player, 2024, 1, 1, 0);
					player.broadcastPacket(MSU);

					player.broadcastUserInfo();
				}
				break;
			}
			case 2:
			{
				if (pvpchest == null)
					return;
				
				if (pvpchest.getEnchantLevel() >= Config.CHECK_MAX_ENCHANT_ARMOR_JEWELS)
					return;

				if (!Config.ARMOR_LIST_ID_ENCHANT_RESTRICT.contains(pvpchest.getItemId()))
					return;
				
				if (pvpchest.getEnchantLevel() >= Config.CHECK_MIN_ENCHANT_ARMOR_JEWELS)
				{
					pvpchest.setEnchantLevel(pvpchest.getEnchantLevel() + 1);
					Broadcast.announceEventHideToPlayers(player.getName() + " get an PvP Enchant of " + pvpchest.getItem().getName() + " +" + pvpchest.getEnchantLevel());
					player.sendPacket(new ItemList(player, false));

					MagicSkillUse MSU = new MagicSkillUse(player, player, 2024, 1, 1, 0);
					player.broadcastPacket(MSU);

					player.broadcastUserInfo();
				}
				break;
			}
//			case 3:
//			{
//				if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) != null)
//				{
//					if (pvplegs == null)
//						return;
//					
//					if (pvplegs.getEnchantLevel() >= Config.CHECK_MAX_ENCHANT_ARMOR_JEWELS)
//						return;
//
//					if (!Config.ARMOR_LIST_ID_ENCHANT_RESTRICT.contains(pvplegs.getItemId()))
//						return;
//					
//					if (pvplegs.getEnchantLevel() >= Config.CHECK_MIN_ENCHANT_ARMOR_JEWELS)
//					{
//						pvplegs.setEnchantLevel(pvplegs.getEnchantLevel() + 1);
//						Broadcast.announceEventHideToPlayers(player.getName() + " get an PvP Enchant of " + pvplegs.getItem().getName() + " +" + pvplegs.getEnchantLevel());
//						player.sendPacket(new ItemList(player, false));
//
//						MagicSkillUse MSU = new MagicSkillUse(player, player, 2024, 1, 1, 0);
//						player.broadcastPacket(MSU);
//
//						player.broadcastUserInfo();
//					}
//					break;
//				}
//				else
//				{
//					if (pvpchest == null)
//						return;
//					
//					if (pvpchest.getEnchantLevel() >= Config.CHECK_MAX_ENCHANT_ARMOR_JEWELS)
//						return;
//
//					if (!Config.ARMOR_LIST_ID_ENCHANT_RESTRICT.contains(pvpchest.getItemId()))
//						return;
//					
//					if (pvpchest.getEnchantLevel() >= Config.CHECK_MIN_ENCHANT_ARMOR_JEWELS)
//					{
//						pvpchest.setEnchantLevel(pvpchest.getEnchantLevel() + 1);
//						Broadcast.announceEventHideToPlayers(player.getName() + " get an PvP Enchant of " + pvpchest.getItem().getName() + " +" + pvpchest.getEnchantLevel());
//						player.sendPacket(new ItemList(player, false));
//
//						MagicSkillUse MSU = new MagicSkillUse(player, player, 2024, 1, 1, 0);
//						player.broadcastPacket(MSU);
//
//						player.broadcastUserInfo();
//					}
//					break;
//				}
//			}
			case 3:
			{
			    if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) != null)
			    {
			        if (pvplegs == null)
			            return;

			        if (pvplegs.getEnchantLevel() >= Config.CHECK_MAX_ENCHANT_ARMOR_JEWELS)
			            return;

			        if (!Config.ARMOR_LIST_ID_ENCHANT_RESTRICT.contains(pvplegs.getItemId()))
			            return;

			        if (pvplegs.getEnchantLevel() >= Config.CHECK_MIN_ENCHANT_ARMOR_JEWELS)
			        {
			            pvplegs.setEnchantLevel(pvplegs.getEnchantLevel() + 1);
			            Broadcast.announceEventHideToPlayers(player.getName() + " get an PvP Enchant of " + pvplegs.getItem().getName() + " +" + pvplegs.getEnchantLevel());
			            player.sendPacket(new ItemList(player, false));

			            MagicSkillUse MSU = new MagicSkillUse(player, player, 2024, 1, 1, 0);
			            player.broadcastPacket(MSU);

			            player.broadcastUserInfo();
			        }
			    }
			    else
			    {
			        if (pvpchest == null)
			            return;

			        if (pvpchest.getEnchantLevel() >= Config.CHECK_MAX_ENCHANT_ARMOR_JEWELS)
			            return;

			        if (!Config.ARMOR_LIST_ID_ENCHANT_RESTRICT.contains(pvpchest.getItemId()))
			            return;

			        if (pvpchest.getEnchantLevel() >= Config.CHECK_MIN_ENCHANT_ARMOR_JEWELS)
			        {
			            pvpchest.setEnchantLevel(pvpchest.getEnchantLevel() + 1);
			            Broadcast.announceEventHideToPlayers(player.getName() + " get an PvP Enchant of " + pvpchest.getItem().getName() + " +" + pvpchest.getEnchantLevel());
			            player.sendPacket(new ItemList(player, false));

			            MagicSkillUse MSU = new MagicSkillUse(player, player, 2024, 1, 1, 0);
			            player.broadcastPacket(MSU);

			            player.broadcastUserInfo();
			        }
			    }
			    break;
			}

			case 4:
			{
				if (pvpfeet == null)
					return;
				
				if (pvpfeet.getEnchantLevel() >= Config.CHECK_MAX_ENCHANT_ARMOR_JEWELS)
					return;

				if (!Config.ARMOR_LIST_ID_ENCHANT_RESTRICT.contains(pvpfeet.getItemId()))
					return;
				
				if (pvpfeet.getEnchantLevel() >= Config.CHECK_MIN_ENCHANT_ARMOR_JEWELS)
				{
					pvpfeet.setEnchantLevel(pvpfeet.getEnchantLevel() + 1);
					Broadcast.announceEventHideToPlayers(player.getName() + " get an PvP Enchant of " + pvpfeet.getItem().getName() + " +" + pvpfeet.getEnchantLevel());
					player.sendPacket(new ItemList(player, false));

					MagicSkillUse MSU = new MagicSkillUse(player, player, 2024, 1, 1, 0);
					player.broadcastPacket(MSU);

					player.broadcastUserInfo();
				}
				break;
			}
			case 5:
			{
				if (pvpneck == null)
					return;
				
				if (pvpneck.getEnchantLevel() >= Config.CHECK_MAX_ENCHANT_ARMOR_JEWELS)
					return;

				if (!Config.ARMOR_LIST_ID_ENCHANT_RESTRICT.contains(pvpneck.getItemId()))
					return;
				
				if (pvpneck.getEnchantLevel() >= Config.CHECK_MIN_ENCHANT_ARMOR_JEWELS)
				{
					pvpneck.setEnchantLevel(pvpneck.getEnchantLevel() + 1);
					Broadcast.announceEventHideToPlayers(player.getName() + " get an PvP Enchant of " + pvpneck.getItem().getName() + " +" + pvpneck.getEnchantLevel());
					player.sendPacket(new ItemList(player, false));

					MagicSkillUse MSU = new MagicSkillUse(player, player, 2024, 1, 1, 0);
					player.broadcastPacket(MSU);

					player.broadcastUserInfo();
				}
				break;
			}
			case 6:
			{
				if (pvplf == null)
					return;
				
				if (pvplf.getEnchantLevel() >= Config.CHECK_MAX_ENCHANT_ARMOR_JEWELS)
					return;

				if (!Config.ARMOR_LIST_ID_ENCHANT_RESTRICT.contains(pvplf.getItemId()))
					return;
				
				if (pvplf.getEnchantLevel() >= Config.CHECK_MIN_ENCHANT_ARMOR_JEWELS)
				{
					pvplf.setEnchantLevel(pvplf.getEnchantLevel() + 1);
					Broadcast.announceEventHideToPlayers(player.getName() + " get an PvP Enchant of " + pvplf.getItem().getName() + " +" + pvplf.getEnchantLevel());
					player.sendPacket(new ItemList(player, false));

					MagicSkillUse MSU = new MagicSkillUse(player, player, 2024, 1, 1, 0);
					player.broadcastPacket(MSU);

					player.broadcastUserInfo();
				}
				break;
			}
			case 7:
			{
				if (pvprf == null)
					return;
				
				if (pvprf.getEnchantLevel() >= Config.CHECK_MAX_ENCHANT_ARMOR_JEWELS)
					return;

				if (!Config.ARMOR_LIST_ID_ENCHANT_RESTRICT.contains(pvprf.getItemId()))
					return;
				
				if (pvprf.getEnchantLevel() >= Config.CHECK_MIN_ENCHANT_ARMOR_JEWELS)
				{
					pvprf.setEnchantLevel(pvprf.getEnchantLevel() + 1);
					Broadcast.announceEventHideToPlayers(player.getName() + " get an PvP Enchant of " + pvprf.getItem().getName() + " +" + pvprf.getEnchantLevel());
					player.sendPacket(new ItemList(player, false));

					MagicSkillUse MSU = new MagicSkillUse(player, player, 2024, 1, 1, 0);
					player.broadcastPacket(MSU);

					player.broadcastUserInfo();
				}
				break;
			}
			case 8:
			{
				if (pvple == null)
					return;
				
				if (pvple.getEnchantLevel() >= Config.CHECK_MAX_ENCHANT_ARMOR_JEWELS)
					return;

				if (!Config.ARMOR_LIST_ID_ENCHANT_RESTRICT.contains(pvple.getItemId()))
					return;
				
				if (pvple.getEnchantLevel() >= Config.CHECK_MIN_ENCHANT_ARMOR_JEWELS)
				{
					pvple.setEnchantLevel(pvple.getEnchantLevel() + 1);
					Broadcast.announceEventHideToPlayers(player.getName() + " get an PvP Enchant of " + pvple.getItem().getName() + " +" + pvple.getEnchantLevel());
					player.sendPacket(new ItemList(player, false));

					MagicSkillUse MSU = new MagicSkillUse(player, player, 2024, 1, 1, 0);
					player.broadcastPacket(MSU);

					player.broadcastUserInfo();
				}
				break;
			}
			case 9:
			{
				if (pvpra == null)
					return;
				
				if (pvpra.getEnchantLevel() >= Config.CHECK_MAX_ENCHANT_ARMOR_JEWELS)
					return;

				if (!Config.ARMOR_LIST_ID_ENCHANT_RESTRICT.contains(pvpra.getItemId()))
					return;
				
				if (pvpra.getEnchantLevel() >= Config.CHECK_MIN_ENCHANT_ARMOR_JEWELS)
				{
					pvpra.setEnchantLevel(pvpra.getEnchantLevel() + 1);
					Broadcast.announceEventHideToPlayers(player.getName() + " get an PvP Enchant of " + pvpra.getItem().getName() + " +" + pvpra.getEnchantLevel());
					player.sendPacket(new ItemList(player, false));

					MagicSkillUse MSU = new MagicSkillUse(player, player, 2024, 1, 1, 0);
					player.broadcastPacket(MSU);

					player.broadcastUserInfo();
				}
				break;
			}
		}
	}
}