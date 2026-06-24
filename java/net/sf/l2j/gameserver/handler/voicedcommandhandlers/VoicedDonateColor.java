package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class VoicedDonateColor implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"donateColormanager",
		"donateColor_name_1",
		"donateColor_name_2",
		"donateColor_name_3",
		"donateColor_name_4",
		"donateColor_name_5",
		"donateColor_name_6",
		"donateColor_name_7",
		"donateColor_name_8",
		"donateColor_name_9",
		"donateColor_name_l",
		"donateColor_name_v",
		"donateColor_name_r",
		"donateColor_name_b",
		"donateColor_name_p",
		"donateColor_name_g",
		"donateColor_title_1",
		"donateColor_title_2",
		"donateColor_title_3",
		"donateColor_title_4",
		"donateColor_title_5",
		"donateColor_title_6",
		"donateColor_title_7",
		"donateColor_title_8",
		"donateColor_title_9",
		"donateColor_title_l",
		"donateColor_title_v",
		"donateColor_title_r",
		"donateColor_title_b",
		"donateColor_title_p",
		"donateColor_title_g"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (command.startsWith("donateColormanager"))
			showDonateColorHtml(activeChar);
		
		else if (command.startsWith("donateColor_name_1"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setNameColor(0x009900);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_name_2"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setNameColor(0xff7f00);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_name_3"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setNameColor(0x994023);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_name_4"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setNameColor(0x00ffff);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_name_5"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setNameColor(0x4c4c4c);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_name_6"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setNameColor(0x990432);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_name_7"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setNameColor(0x70db93);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_name_8"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setNameColor(0x9f9f9f);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_name_9"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setNameColor(0xffff00);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_name_l"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setNameColor(0x0099ff);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_name_v"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setNameColor(0x330066);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_name_r"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setNameColor(0xff66ff);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		
		else if (command.startsWith("donateColor_name_b"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setNameColor(0x997e00);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_name_p"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setNameColor(0x9900cc);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_name_g"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setNameColor(0x00fb87);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_title_1"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x009900);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_title_2"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setTitleColor(0xff7f00);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_title_3"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x994023);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_title_4"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x00ffff);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_title_5"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x4c4c4c);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_title_6"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x990432);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_title_7"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x70db93);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_title_8"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x9f9f9f);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_title_9"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setTitleColor(0xffff00);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_title_l"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x0099ff);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_title_v"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x330066);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_title_r"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setTitleColor(0xff66ff);
				activeChar.sendMessage("Congratulations, your title's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		
		else if (command.startsWith("donateColor_title_b"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x997e00);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_title_p"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x9900cc);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		else if (command.startsWith("donateColor_title_g"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, 0) >= Config.DONATE_COIN_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_COIN_COUNT, activeChar, null);
				activeChar.getAppearance().setTitleColor(0x00fb87);
				activeChar.sendMessage("Congratulations, your name's color changed successfully!");
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("You do not have enough " + Config.DONATE_COIN_COUNT + " Donate Coin's.");
			}
		}
		return true;
	}
	
	private static void showDonateColorHtml(Player activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/menu/ColorManager_Donate.htm");
		activeChar.sendPacket(html);
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}