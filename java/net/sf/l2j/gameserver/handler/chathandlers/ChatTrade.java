package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.instancemanager.custom.ChatGlobalManager;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.util.FloodProtectors;
import net.sf.l2j.gameserver.util.FloodProtectors.Action;

/*public class ChatTrade implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		8
	};
	
	@Override
	public void handleChat(int type, L2PcInstance activeChar, String target, String text)
	{
		if (Config.DISABLE_CHAT && !activeChar.isGM())
		{
			activeChar.sendMessage("The chat is Temporarily unavailable.");
			return;
		}
		if (!FloodProtectors.performAction(activeChar.getClient(), Action.TRADE_CHAT))
			return;
		
		final CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
		final int region = MapRegionTable.getMapRegion(activeChar.getX(), activeChar.getY());
		
		for (L2PcInstance player : L2World.getInstance().getPlayers())
		{
			if (!BlockList.isBlocked(player, activeChar) && region == MapRegionTable.getMapRegion(player.getX(), player.getY()))
				player.sendPacket(cs);
		}
	}
	
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}*/
public class ChatTrade implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		8
	};
	
	@Override
	public void handleChat(int type, Player activeChar, String target, String text)
	{	
		if (Config.DISABLE_CHAT && !activeChar.isGM())
		{
			activeChar.sendMessage("The chat is Temporarily unavailable.");
			return;
		}
		
		if (activeChar.ChatProtection(activeChar.getHWID()) && activeChar.isChatBlocked() && ((activeChar.getChatBanTimer() - 1500) > System.currentTimeMillis()))
		{
			if (((activeChar.getChatBanTimer() - System.currentTimeMillis()) / 1000) >= 60)
				activeChar.sendChatMessage(0, Say2.TELL, "SYS", "Your chat was suspended for " + (activeChar.getChatBanTimer() - System.currentTimeMillis()) / (1000*60) + " minute(s).");
			else
				activeChar.sendChatMessage(0, Say2.TELL, "SYS", "Your chat was suspended for " + (activeChar.getChatBanTimer() - System.currentTimeMillis()) / 1000 + " second(s).");
			
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		if ((activeChar.getChatGlobalTimer() - 1500) > System.currentTimeMillis())
		{
			activeChar.sendMessage("You must wait " + (activeChar.getChatGlobalTimer() - System.currentTimeMillis()) / 1000 + " seconds to use trade chat.");
			return;
		}
		
		if (!FloodProtectors.performAction(activeChar.getClient(), Action.GLOBAL_CHAT))
			return;
		
		//final CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, Config.MSG_CHAT_TRADE+ activeChar.getName(), text);
		final CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type,  activeChar.getName(), text);
		
		String convert = text.toLowerCase();
		final CreatureSay disable = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), convert);
		
		final int region = MapRegionTable.getMapRegion(activeChar.getX(), activeChar.getY());
		
		if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("ON"))
		{
			for (Player player : L2World.getInstance().getPlayers())
			{
				if (region == MapRegionTable.getMapRegion(player.getX(), player.getY()))
					
					if (Config.DISABLE_CAPSLOCK && !activeChar.isGM())
						player.sendPacket(disable);
					else
						player.sendPacket(cs);
				
				if (!activeChar.isGM() && Config.CUSTOM_GLOBAL_CHAT_TIME > 0)
				{
					activeChar.setChatGlobalTimer(System.currentTimeMillis() + Config.CUSTOM_GLOBAL_CHAT_TIME * 1000);
					if (!ChatGlobalManager.getInstance().hasChatPrivileges(activeChar.getObjectId()))
						ChatGlobalManager.getInstance().addChatTime(activeChar.getObjectId(), System.currentTimeMillis() + Config.CUSTOM_GLOBAL_CHAT_TIME * 1000);
				}
			}
		}
		else if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("GLOBAL"))
		{
			if (Config.TRADE_CHAT_WITH_PVP)
			{
				if ((activeChar.getPvpKills() < Config.TRADE_PVP_AMOUNT) && !activeChar.isGM())
				{
					activeChar.sendMessage("You must have at least " + Config.TRADE_PVP_AMOUNT + " pvp kills in order to speak in trade chat");
					return;
				}
				
				for (Player player : L2World.getInstance().getPlayers())
				{
					if (Config.DISABLE_CAPSLOCK && !activeChar.isGM())
						player.sendPacket(disable);
					else
						player.sendPacket(cs);
					
					if (!activeChar.isGM() && Config.CUSTOM_GLOBAL_CHAT_TIME > 0)
					{
						activeChar.setChatGlobalTimer(System.currentTimeMillis() + Config.CUSTOM_GLOBAL_CHAT_TIME * 1000);
						if (!ChatGlobalManager.getInstance().hasChatPrivileges(activeChar.getObjectId()))
							ChatGlobalManager.getInstance().addChatTime(activeChar.getObjectId(), System.currentTimeMillis() + Config.CUSTOM_GLOBAL_CHAT_TIME * 1000);
					}
				}
				
			}
			else
			{
				for (Player player : L2World.getInstance().getPlayers())
				{
					if (Config.DISABLE_CAPSLOCK && !activeChar.isGM())
						player.sendPacket(disable);
					else
						player.sendPacket(cs);
					
					if (!activeChar.isGM() && Config.CUSTOM_GLOBAL_CHAT_TIME > 0)
					{
						activeChar.setChatGlobalTimer(System.currentTimeMillis() + Config.CUSTOM_GLOBAL_CHAT_TIME * 1000);
						if (!ChatGlobalManager.getInstance().hasChatPrivileges(activeChar.getObjectId()))
							ChatGlobalManager.getInstance().addChatTime(activeChar.getObjectId(), System.currentTimeMillis() + Config.CUSTOM_GLOBAL_CHAT_TIME * 1000);
					}
				}
				
			}
		}
	}
	
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}