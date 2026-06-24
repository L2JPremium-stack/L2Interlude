package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class VoicedNewColor implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"colorname",
		"colortitle",
		"bcolor_name_1",
		"bcolor_name_2",
		"bcolor_name_3",
		"bcolor_name_4",
		"bcolor_name_5",
		"bcolor_name_6",
		"bcolor_name_7",
		"bcolor_name_8",
		"bcolor_name_9",
		"bcolor_name_l",
		"bcolor_name_v",
		"bcolor_name_r",
		"bcolor_name_b",
		"bcolor_name_p",
		"bcolor_name_g",
		"bcolor_title_1",
		"bcolor_title_2",
		"bcolor_title_3",
		"bcolor_title_4",
		"bcolor_title_5",
		"bcolor_title_6",
		"bcolor_title_7",
		"bcolor_title_8",
		"bcolor_title_9",
		"bcolor_title_l",
		"bcolor_title_v",
		"bcolor_title_r",
		"bcolor_title_b",
		"bcolor_title_p",
		"bcolor_title_g"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (command.startsWith("colorname"))
			sendHtmlName(activeChar);
		
		else if (command.startsWith("colortitle"))
			sendHtmlTitle(activeChar);
		
		// Name
		// 1000 Pvp's 200 Kill Points
		else if (command.startsWith("bcolor_name_1"))
		{
			if (activeChar.getPvpKills() <= 1000)
			{
				activeChar.sendMessage("You do not have enough 1000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 200)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 200, activeChar, null);
				activeChar.getAppearance().setNameColor(0x009900);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 200 Kill Point's.");
			}
		}
		else if (command.startsWith("bcolor_name_2"))
		{
			if (activeChar.getPvpKills() <= 1000)
			{
				activeChar.sendMessage("You do not have enough 1000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 200)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 200, activeChar, null);
				activeChar.getAppearance().setNameColor(0xff7f00);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 200 Kill Point's.");
			}
		}
		else if (command.startsWith("bcolor_name_3"))
		{
			if (activeChar.getPvpKills() <= 1000)
			{
				activeChar.sendMessage("You do not have enough 1000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 200)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 200, activeChar, null);
				activeChar.getAppearance().setNameColor(0x994023);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 200 Kill Point's.");
			}
		}
		// 2000 Pvp's 400 Kill Points
		else if (command.startsWith("bcolor_name_4"))
		{
			if (activeChar.getPvpKills() <= 2000)
			{
				activeChar.sendMessage("You do not have enough 2000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 400)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 400, activeChar, null);
				activeChar.getAppearance().setNameColor(0x00ffff);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 400 Kill Point's.");
			}
		}
		else if (command.startsWith("bcolor_name_5"))
		{
			if (activeChar.getPvpKills() <= 2000)
			{
				activeChar.sendMessage("You do not have enough 2000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 400)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 400, activeChar, null);
				activeChar.getAppearance().setNameColor(0x4c4c4c);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 400 Kill Point's.");
			}
		}
		else if (command.startsWith("bcolor_name_6"))
		{
			if (activeChar.getPvpKills() <= 2000)
			{
				activeChar.sendMessage("You do not have enough 2000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 400)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 400, activeChar, null);
				activeChar.getAppearance().setNameColor(0x990432);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 400 Kill Point's.");
			}
		}
		// 4000 Pvp's 800 Kill Points
		else if (command.startsWith("bcolor_name_7"))
		{
			if (activeChar.getPvpKills() <= 4000)
			{
				activeChar.sendMessage("You do not have enough 4000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 800)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 800, activeChar, null);
				activeChar.getAppearance().setNameColor(0x70db93);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 800 Kill Point's.");
			}
		}
		else if (command.startsWith("bcolor_name_8"))
		{
			if (activeChar.getPvpKills() <= 4000)
			{
				activeChar.sendMessage("You do not have enough 4000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 800)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 800, activeChar, null);
				activeChar.getAppearance().setNameColor(0x9f9f9f);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 800 Kill Point's.");
			}
		}
		else if (command.startsWith("bcolor_name_9"))
		{
			if (activeChar.getPvpKills() <= 4000)
			{
				activeChar.sendMessage("You do not have enough 4000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 800)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 800, activeChar, null);
				activeChar.getAppearance().setNameColor(0xffff00);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 800 Kill Point's.");
			}
		}
		// 8000 Pvp's 1600 Kill Points
		else if (command.startsWith("bcolor_name_l"))
		{
			if (activeChar.getPvpKills() <= 8000)
			{
				activeChar.sendMessage("You do not have enough 8000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 1600)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 1600, activeChar, null);
				activeChar.getAppearance().setNameColor(0x0099ff);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 1600 Kill Point's.");
			}
		}
		else if (command.startsWith("bcolor_name_v"))
		{
			if (activeChar.getPvpKills() <= 8000)
			{
				activeChar.sendMessage("You do not have enough 8000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 1600)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 1600, activeChar, null);
				activeChar.getAppearance().setNameColor(0x330066);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 1600 Kill Point's.");
			}
		}
		else if (command.startsWith("bcolor_name_r"))
		{
			if (activeChar.getPvpKills() <= 8000)
			{
				activeChar.sendMessage("You do not have enough 8000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 1600)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 1600, activeChar, null);
				activeChar.getAppearance().setNameColor(0xff66ff);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 1600 Kill Point's.");
			}
		}
		// 16000 Pvp's 3200 Kill Points
		else if (command.startsWith("bcolor_name_b"))
		{
			if (activeChar.getPvpKills() <= 16000)
			{
				activeChar.sendMessage("You do not have enough 16000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 3200)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 3200, activeChar, null);
				activeChar.getAppearance().setNameColor(0x997e00);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 3200 Kill Point's.");
			}
		}
		else if (command.startsWith("bcolor_name_p"))
		{
			if (activeChar.getPvpKills() <= 16000)
			{
				activeChar.sendMessage("You do not have enough 16000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 3200)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 3200, activeChar, null);
				activeChar.getAppearance().setNameColor(0x9900cc);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 3200 Kill Point's.");
			}
		}
		else if (command.startsWith("bcolor_name_g"))
		{
			if (activeChar.getPvpKills() <= 16000)
			{
				activeChar.sendMessage("You do not have enough 16000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 3200)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 3200, activeChar, null);
				activeChar.getAppearance().setNameColor(0x00fb87);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 3200 Kill Point's.");
			}
		}
		// Title
		// 1000 Pvp's 200 Kill Points
		else if (command.startsWith("bcolor_title_1"))
		{
			if (activeChar.getPvpKills() <= 1000)
			{
				activeChar.sendMessage("You do not have enough 1000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 200)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 200, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x009900);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 200 Kill Point's.");
			}
		}
		else if (command.startsWith("bcolor_title_2"))
		{
			if (activeChar.getPvpKills() <= 1000)
			{
				activeChar.sendMessage("You do not have enough 1000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 200)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 200, activeChar, null);
				activeChar.getAppearance().setTitleColor(0xff7f00);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 200 Kill Point's.");
			}
		}
		else if (command.startsWith("bcolor_title_3"))
		{
			if (activeChar.getPvpKills() <= 1000)
			{
				activeChar.sendMessage("You do not have enough 1000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 200)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 200, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x994023);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 200 Kill Point's.");
			}
		}
		// 2000 Pvp's 400 Kill Points
		else if (command.startsWith("bcolor_title_4"))
		{
			if (activeChar.getPvpKills() <= 2000)
			{
				activeChar.sendMessage("You do not have enough 2000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 400)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 400, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x00ffff);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 400 Kill Point's.");
			}
		}
		else if (command.startsWith("bcolor_title_5"))
		{
			if (activeChar.getPvpKills() <= 2000)
			{
				activeChar.sendMessage("You do not have enough 2000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 400)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 400, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x4c4c4c);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 400 Kill Point's.");
			}
		}
		else if (command.startsWith("bcolor_title_6"))
		{
			if (activeChar.getPvpKills() <= 2000)
			{
				activeChar.sendMessage("You do not have enough 2000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 400)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 400, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x990432);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 400 Kill Point's.");
			}
		}
		// 4000 Pvp's 800 Kill Points
		else if (command.startsWith("bcolor_title_7"))
		{
			if (activeChar.getPvpKills() <= 4000)
			{
				activeChar.sendMessage("You do not have enough 4000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 800)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 800, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x70db93);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 800 Kill Point's.");
			}
		}
		else if (command.startsWith("bcolor_title_8"))
		{
			if (activeChar.getPvpKills() <= 4000)
			{
				activeChar.sendMessage("You do not have enough 4000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 800)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 800, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x9f9f9f);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 800 Kill Point's.");
			}
		}
		else if (command.startsWith("bcolor_title_9"))
		{
			if (activeChar.getPvpKills() <= 4000)
			{
				activeChar.sendMessage("You do not have enough 4000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 800)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 800, activeChar, null);
				activeChar.getAppearance().setTitleColor(0xffff00);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 800 Kill Point's.");
			}
		}
		// 8000 Pvp's 1600 Kill Points
		else if (command.startsWith("bcolor_title_l"))
		{
			if (activeChar.getPvpKills() <= 8000)
			{
				activeChar.sendMessage("You do not have enough 8000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 1600)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 1600, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x0099ff);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 1600 Kill Point's.");
			}
		}
		else if (command.startsWith("bcolor_title_v"))
		{
			if (activeChar.getPvpKills() <= 8000)
			{
				activeChar.sendMessage("You do not have enough 8000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 1600)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 1600, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x330066);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 1600 Kill Point's.");
			}
		}
		else if (command.startsWith("bcolor_title_r"))
		{
			if (activeChar.getPvpKills() <= 8000)
			{
				activeChar.sendMessage("You do not have enough 8000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 1600)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 1600, activeChar, null);
				activeChar.getAppearance().setTitleColor(0xff66ff);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 1600 Kill Point's.");
			}
		}
		// 16000 Pvp's 3200 Kill Points
		else if (command.startsWith("bcolor_title_b"))
		{
			if (activeChar.getPvpKills() <= 16000)
			{
				activeChar.sendMessage("You do not have enough 16000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 3200)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 3200, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x997e00);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 3200 Kill Point's.");
			}
		}
		else if (command.startsWith("bcolor_title_p"))
		{
			if (activeChar.getPvpKills() <= 16000)
			{
				activeChar.sendMessage("You do not have enough 16000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 3200)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 3200, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x9900cc);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 3200 Kill Point's.");
			}
		}
		else if (command.startsWith("bcolor_title_g"))
		{
			if (activeChar.getPvpKills() <= 16000)
			{
				activeChar.sendMessage("You do not have enough 16000 Pvp's.");
				return false;
			}
			else if (activeChar.getInventory().getInventoryItemCount(Config.PVP_POINT_ID, 0) >= 3200)
			{
				activeChar.getInventory().destroyItemByItemId("Kill Point", Config.PVP_POINT_ID, 3200, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x00fb87);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough 3200 Kill Point's.");
			}
		}
		return true;
	}
	
	private static void sendHtmlName(Player activeChar)
	{
		String htmFile = "data/html/mods/menu/ColorName.htm";
		
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setFile(htmFile);
		activeChar.sendPacket(msg);
	}
	
	private static void sendHtmlTitle(Player activeChar)
	{
		String htmFile = "data/html/mods/menu/ColorTitle.htm";
		
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