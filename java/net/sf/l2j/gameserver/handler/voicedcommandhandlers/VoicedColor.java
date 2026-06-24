package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class VoicedColor implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"colormanager",
		"color_name_1",
		"color_name_2",
		"color_name_3",
		"color_name_4",
		"color_name_5",
		"color_name_6",
		"color_name_7",
		"color_name_8",
		"color_name_9",
		"color_name_l",
		"color_name_v",
		"color_name_r",
		"color_name_b",
		"color_name_p",
		"color_name_g",
		"color_title_1",
		"color_title_2",
		"color_title_3",
		"color_title_4",
		"color_title_5",
		"color_title_6",
		"color_title_7",
		"color_title_8",
		"color_title_9",
		"color_title_l",
		"color_title_v",
		"color_title_r",
		"color_title_b",
		"color_title_p",
		"color_title_g"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (command.startsWith("colormanager"))
		{
			sendHtml(activeChar);
		}
		else if (command.startsWith("color_name_1"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setNameColor(0x009900);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setNameColor(0x009900);
					activeChar.sendMessage("Congratulations, your name's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_name_2"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setNameColor(0xff7f00);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setNameColor(0xff7f00);
					activeChar.sendMessage("Congratulations, your name's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_name_3"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setNameColor(0x994023);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setNameColor(0x994023);
					activeChar.sendMessage("Congratulations, your name's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_name_4"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setNameColor(0x00ffff);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setNameColor(0x00ffff);
					activeChar.sendMessage("Congratulations, your name's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_name_5"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setNameColor(0x4c4c4c);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setNameColor(0x4c4c4c);
					activeChar.sendMessage("Congratulations, your name's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_name_6"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setNameColor(0x990432);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setNameColor(0x990432);
					activeChar.sendMessage("Congratulations, your name's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_name_7"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setNameColor(0x70db93);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setNameColor(0x70db93);
					activeChar.sendMessage("Congratulations, your name's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_name_8"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setNameColor(0x9f9f9f);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setNameColor(0x9f9f9f);
					activeChar.sendMessage("Congratulations, your name's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_name_9"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setNameColor(0xffff00);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setNameColor(0xffff00);
					activeChar.sendMessage("Congratulations, your name's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_name_l"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setNameColor(0x0099ff);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setNameColor(0x0099ff);
					activeChar.sendMessage("Congratulations, your name's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_name_v"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setNameColor(0x330066);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setNameColor(0x330066);
					activeChar.sendMessage("Congratulations, your name's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_name_r"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setNameColor(0xff66ff);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setNameColor(0xff66ff);
					activeChar.sendMessage("Congratulations, your name's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_name_b"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setNameColor(0x997e00);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setNameColor(0x997e00);
					activeChar.sendMessage("Congratulations, your name's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_name_p"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setNameColor(0x9900cc);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setNameColor(0x9900cc);
					activeChar.sendMessage("Congratulations, your name's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_name_g"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setNameColor(0x00fb87);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setNameColor(0x00fb87);
					activeChar.sendMessage("Congratulations, your name's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_title_1"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setTitleColor(0x009900);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setTitleColor(0x009900);
					activeChar.sendMessage("Congratulations, your title's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_title_2"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setTitleColor(0xff7f00);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setTitleColor(0xff7f00);
					activeChar.sendMessage("Congratulations, your title's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_title_3"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setTitleColor(0x994023);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setTitleColor(0x994023);
					activeChar.sendMessage("Congratulations, your title's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_title_4"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setTitleColor(0x00ffff);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setTitleColor(0x00ffff);
					activeChar.sendMessage("Congratulations, your title's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_title_5"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setTitleColor(0x4c4c4c);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setTitleColor(0x4c4c4c);
					activeChar.sendMessage("Congratulations, your title's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_title_6"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setTitleColor(0x990432);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setTitleColor(0x990432);
					activeChar.sendMessage("Congratulations, your title's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_title_7"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setTitleColor(0x70db93);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setTitleColor(0x70db93);
					activeChar.sendMessage("Congratulations, your title's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_title_8"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setTitleColor(0x9f9f9f);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setTitleColor(0x9f9f9f);
					activeChar.sendMessage("Congratulations, your title's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_title_9"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setTitleColor(0xffff00);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setTitleColor(0xffff00);
					activeChar.sendMessage("Congratulations, your title's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_title_l"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setTitleColor(0x0099ff);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setTitleColor(0x0099ff);
					activeChar.sendMessage("Congratulations, your title's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_title_v"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setTitleColor(0x330066);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setTitleColor(0x330066);
					activeChar.sendMessage("Congratulations, your title's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_title_r"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setTitleColor(0xff66ff);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setTitleColor(0xff66ff);
					activeChar.sendMessage("Congratulations, your title's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_title_b"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setTitleColor(0x997e00);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setTitleColor(0x997e00);
					activeChar.sendMessage("Congratulations, your name's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_title_p"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setTitleColor(0x9900cc);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setTitleColor(0x9900cc);
					activeChar.sendMessage("Congratulations, your name's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		else if (command.startsWith("color_title_g"))
		{
			if (activeChar.isVip())
			{
				activeChar.getAppearance().setTitleColor(0x00fb87);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= Config.PVP_POINT_COUNT)
				{
					activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, Config.PVP_POINT_COUNT, activeChar, null);
					activeChar.getAppearance().setTitleColor(0x00fb87);
					activeChar.sendMessage("Congratulations, your name's color changed successfully!");
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("You do not have enough " + Config.PVP_POINT_COUNT + " Kill Point's.");
				}
			}
		}
		return true;
	}
	
	private static void sendHtml(Player activeChar)
	{
		String htmFile = "data/html/mods/menu/ColorManager.htm";
		
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setFile(htmFile);
		activeChar.sendPacket(msg);
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}