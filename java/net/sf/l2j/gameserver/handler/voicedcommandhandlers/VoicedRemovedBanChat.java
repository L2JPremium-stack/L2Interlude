package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

/**
 * @author Christian
 *
 */
public class VoicedRemovedBanChat implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"removeChatBan",
	};

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, net.sf.l2j.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
	 */
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String params)
	{
		 if (command.startsWith("removeChatBan") && Config.CHAT_BAN_HTML)
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, -1) < Config.REMOVE_CHAT_BAN_ITEM_COUNT)
			{
				activeChar.sendMessage("You do not have enough Donate Coins.");
			//	showOthersHtml(activeChar);
				return false;
			}
			activeChar.destroyItemByItemId("Consume", Config.DONATE_COIN_ID, Config.REMOVE_CHAT_BAN_ITEM_COUNT, activeChar, true);
			activeChar.setPunishLevel(Player.PunishLevel.NONE, 0);
			activeChar.sendMessage("You Chat Desbanned!.");
			VoicedMenu.showMenuHtml(activeChar);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IVoicedCommandHandler#getVoicedCommandList()
	 */
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
}
