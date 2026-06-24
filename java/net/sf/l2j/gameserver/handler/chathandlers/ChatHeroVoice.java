package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.instancemanager.custom.ChatHeroManager;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.util.FloodProtectors;
import net.sf.l2j.gameserver.util.FloodProtectors.Action;

public class ChatHeroVoice implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		17
	};
	
	@Override
	public void handleChat(int type, Player activeChar, String target, String text)
	{
		
		if (Config.DISABLE_CHAT && !activeChar.isGM())
		{
			activeChar.sendMessage("The chat is Temporarily unavailable.");
			return;
		}
		
		if (activeChar.ChatProtection(activeChar.getHWID()) && activeChar.isChatBlocked()&& ((activeChar.getChatBanTimer()-1500) > System.currentTimeMillis()))
		{
			if (((activeChar.getChatBanTimer() - System.currentTimeMillis()) / 1000) >= 60)
				activeChar.sendChatMessage(0, Say2.TELL, "SYS", "Your chat was suspended for " + (activeChar.getChatBanTimer() - System.currentTimeMillis()) / (1000*60) + " minute(s).");
			else
				activeChar.sendChatMessage(0, Say2.TELL, "SYS", "Your chat was suspended for " + (activeChar.getChatBanTimer() - System.currentTimeMillis()) / 1000 + " second(s).");
			
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		if (!activeChar.isHero() && !activeChar.isGM())
			return;
		
		if ((activeChar.getChatHeroTimer()-1500) > System.currentTimeMillis())
		{
			activeChar.sendMessage("You must wait " + (activeChar.getChatHeroTimer() - System.currentTimeMillis()) / 1000 + " seconds to use Hero chat.");
			return;
		}
		
		if (!FloodProtectors.performAction(activeChar.getClient(), Action.HERO_VOICE))
			return;
		
		final CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
		
		String convert = text.toLowerCase();
		final CreatureSay disable = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), convert);
		
		for (Player player : L2World.getInstance().getPlayers())
			if (Config.DISABLE_CAPSLOCK && !activeChar.isGM())
				player.sendPacket(disable);
			else
				player.sendPacket(cs);
		
		if (!activeChar.isGM() && Config.CUSTOM_HERO_CHAT_TIME > 0)
		{
			activeChar.setChatHeroTimer(System.currentTimeMillis() + Config.CUSTOM_HERO_CHAT_TIME * 1000);
			if (!ChatHeroManager.getInstance().hasChatPrivileges(activeChar.getObjectId()))
				ChatHeroManager.getInstance().addChatTime(activeChar.getObjectId(), System.currentTimeMillis() + Config.CUSTOM_HERO_CHAT_TIME * 1000);
		}
	}
	
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}