package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.ChatHandler;
import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedMenu;
import net.sf.l2j.gameserver.instancemanager.custom.ChatVipManager;
import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Player.PunishLevel;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.util.FloodProtectors;
import net.sf.l2j.gameserver.util.FloodProtectors.Action;
import net.sf.l2j.gameserver.util.IllegalPlayerAction;
import net.sf.l2j.gameserver.util.Util;

public final class Say2 extends L2GameClientPacket
{
	private static Logger _logChat = Logger.getLogger("chat");
	
	public static final int ALL = 0;
	public static final int SHOUT = 1; // !
	public static final int TELL = 2;
	public static final int PARTY = 3; // #
	public static final int CLAN = 4; // @
	public static final int GM = 5;
	public static final int PETITION_PLAYER = 6;
	public static final int PETITION_GM = 7;
	public static final int TRADE = 8; // +
	public static final int ALLIANCE = 9; // $
	public static final int ANNOUNCEMENT = 10;
	public static final int BOAT = 11;
	public static final int L2FRIEND = 12;
	public static final int MSNCHAT = 13;
	public static final int PARTYMATCH_ROOM = 14;
	public static final int PARTYROOM_COMMANDER = 15; // (Yellow)
	public static final int PARTYROOM_ALL = 16; // (Red)
	public static final int HERO_VOICE = 17;
	public static final int CRITICAL_ANNOUNCE = 18;
	
	public static final String[] CHAT_NAMES =
	{
		"ALL",
		"SHOUT",
		"TELL",
		"PARTY",
		"CLAN",
		"GM",
		"PETITION_PLAYER",
		"PETITION_GM",
		"TRADE",
		"ALLIANCE",
		"ANNOUNCEMENT", // 10
		"BOAT",
		"WILLCRASHCLIENT:)",
		"FAKEALL?",
		"PARTYMATCH_ROOM",
		"PARTYROOM_COMMANDER",
		"PARTYROOM_ALL",
		"HERO_VOICE",
		"CRITICAL_ANNOUNCEMENT"
	};
	
	private static final String[] WALKER_COMMAND_LIST =
	{
		"USESKILL",
		"USEITEM",
		"BUYITEM",
		"SELLITEM",
		"SAVEITEM",
		"LOADITEM",
		"MSG",
		"DELAY",
		"LABEL",
		"JMP",
		"CALL",
		"RETURN",
		"MOVETO",
		"NPCSEL",
		"NPCDLG",
		"DLGSEL",
		"CHARSTATUS",
		"POSOUTRANGE",
		"POSINRANGE",
		"GOHOME",
		"SAY",
		"EXIT",
		"PAUSE",
		"STRINDLG",
		"STRNOTINDLG",
		"CHANGEWAITTYPE",
		"FORCEATTACK",
		"ISMEMBER",
		"REQUESTJOINPARTY",
		"REQUESTOUTPARTY",
		"QUITPARTY",
		"MEMBERSTATUS",
		"CHARBUFFS",
		"ITEMCOUNT",
		"FOLLOWTELEPORT"
	};
	
	private String _text;
	private int _type;
	private String _target;
	
	@Override
	protected void readImpl()
	{
		_text = readS();
		_type = readD();
		_target = (_type == TELL) ? readS() : null;
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		// Say Filter implementation
		if(Config.USE_SAY_FILTER)
		{
			checkText(activeChar);
		}
		
		if (_type < 0 || _type >= CHAT_NAMES.length)
		{
			_log.warning("Say2: Invalid type: " + _type + " Player : " + activeChar.getName() + " text: " + _text);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			activeChar.logout();
			return;
		}
		
		if (_text.isEmpty())
		{
			_log.warning(activeChar.getName() + ": sending empty text. Possible packet hack.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			activeChar.logout();
			return;
		}
		
		if (_text.length() >= 100)
			return;
		if (_text.contains("\\n"))
		{
			activeChar.sendPacket(SystemMessageId.CHATTING_PROHIBITED);
			return;
		}
		if ((activeChar.getChatAllTimer() - 1500) > System.currentTimeMillis() && Config.TALK_CHAT_ALL_CONFIG && !(_type == PARTY) && !(_type == CLAN) && !(_type == ALLIANCE) && !activeChar.isGM())
		{
			VoicedMenu.showMenuHtml(activeChar);
			
			if (((activeChar.getChatAllTimer() - System.currentTimeMillis()) / 1000) >= 60)
			{
			//	activeChar.sendMessage("You need " + Config.TALK_CHAT_ALL_TIME + " minutes online to talk on chat. Finish in " + (activeChar.getChatAllTimer() - System.currentTimeMillis()) / (1000 * 60) + " minute(s).");
				activeChar.sendPacket(new ExShowScreenMessage("You can talk in " + (activeChar.getChatAllTimer() - System.currentTimeMillis()) / (1000 * 60) + " minute(s)", 5 * 1000));
			}
			else
			{
			//	activeChar.sendMessage("You need " + Config.TALK_CHAT_ALL_TIME + " minutes online to talk on chat. Finish in " + (activeChar.getChatAllTimer() - System.currentTimeMillis()) / 1000 + " second(s).");
				activeChar.sendPacket(new ExShowScreenMessage("You can talk in " + (activeChar.getChatAllTimer() - System.currentTimeMillis()) / 1000 + " second(s)", 5 * 1000));
			}
			return;
		}
		if (Config.L2WALKER_PROTECTION && _type == TELL && checkBot(_text))
		{
			Util.handleIllegalPlayerAction(activeChar, "Client Emulator Detect: " + activeChar.getName() + " is using L2Walker.", Config.DEFAULT_PUNISH);
			return;
		}
	
		
		if (!activeChar.isGM() && _type == ANNOUNCEMENT)
		{
			Util.handleIllegalPlayerAction(activeChar, activeChar.getName() + " tried to announce without GM statut.", IllegalPlayerAction.PUNISH_BROADCAST);
			_log.warning(activeChar.getName() + " tried to use announcements without GM statut.");
			return;
		}
		if (_text.startsWith(">") && activeChar.isVip() && !activeChar.isInJail() && !(activeChar.getChatAllTimer() - 1500 > System.currentTimeMillis() && Config.TALK_CHAT_ALL_CONFIG))
		{
			
			String reformatedText = (_text).substring(1);
			CreatureSay pcs = new CreatureSay(activeChar.getObjectId(), Config.COR_CHAT_VIP, activeChar.getName()+" "+"[VIP]", reformatedText);
			if ((activeChar.getChatVipTimer()-1500) > System.currentTimeMillis())
			{
				activeChar.sendMessage("You must wait " + (activeChar.getChatVipTimer() - System.currentTimeMillis()) / 1000 + " seconds to use VIP chat.");
				return;
			}
			if (!FloodProtectors.performAction(activeChar.getClient(), Action.GLOBAL_VIP))
				return;
			for (Player player : L2World.getInstance().getPlayers())
			{
				if (!BlockList.isBlocked(player, activeChar))
				{
					player.sendPacket(pcs);
				}
			}
			if (Config.GLOBAL_VIP_TIME > 0)
			{
				activeChar.setChatVipTimer(System.currentTimeMillis() + Config.GLOBAL_VIP_TIME * 1000);
				if (!ChatVipManager.getInstance().hasChatPrivileges(activeChar.getObjectId()))
					ChatVipManager.getInstance().addChatTime(activeChar.getObjectId(), System.currentTimeMillis() + Config.GLOBAL_VIP_TIME * 1000);
			}
		}

		if (activeChar.isChatBanned() || (activeChar.isInJail() && !activeChar.isGM()))
		{
			if (Config.CHAT_BAN_HTML)
			{
				activeChar.sendPacket(new ExShowScreenMessage(Config.CHAT_BAN_MESSAGE, 5000, ExShowScreenMessage.SMPOS.TOP_CENTER, true));
			    NpcHtmlMessage html = new NpcHtmlMessage(0);
			    html.setFile("data/html/mods/menu/ChatDesBan.htm");
			    activeChar.sendPacket(html);
			    return;	
			}
			activeChar.sendPacket(SystemMessageId.CHATTING_PROHIBITED);
			return;
			
		}
		
		if (_type == PETITION_PLAYER && activeChar.isGM())
			_type = PETITION_GM;
		
		if (Config.LOG_CHAT)
		{
			LogRecord record = new LogRecord(Level.INFO, _text);
			record.setLoggerName("chat");
			
			if (_type == TELL)
				record.setParameters(new Object[]
				{
					CHAT_NAMES[_type],
					"[" + activeChar.getName() + " to " + _target + "]"
				});
			else
				record.setParameters(new Object[]
				{
					CHAT_NAMES[_type],
					"[" + activeChar.getName() + "]"
				});
			
			_logChat.log(record);
		}
		
		_text = _text.replaceAll("\\\\n", "");
		
		IChatHandler handler = ChatHandler.getInstance().getChatHandler(_type);
		if (handler != null)
			handler.handleChat(_type, activeChar, _target, _text);
		else
			_log.warning(activeChar.getName() + " tried to use unregistred chathandler type: " + _type + ".");
	}
	
	private static boolean checkBot(String text)
	{
		for (String botCommand : WALKER_COMMAND_LIST)
		{
			if (text.startsWith(botCommand))
				return true;
		}
		return false;
	}
	private void checkText(Player activeChar)
	{
		if(Config.USE_SAY_FILTER)
		{
			String filteredText = _text.toLowerCase();
			
			for(String pattern : Config.FILTER_LIST)
			{
				filteredText = filteredText.replaceAll("(?i)" + pattern, Config.CHAT_FILTER_CHARS);
			}
			
			if(!filteredText.equalsIgnoreCase(_text))
			{				
				if(Config.CHAT_FILTER_PUNISHMENT.equalsIgnoreCase("chat"))
				{
					activeChar.setPunishLevel(PunishLevel.CHAT, Config.CHAT_FILTER_PUNISHMENT_PARAM1);
				}
				else if(Config.CHAT_FILTER_PUNISHMENT.equalsIgnoreCase("karma"))
				{
					activeChar.setKarma(Config.CHAT_FILTER_PUNISHMENT_PARAM2);
					activeChar.sendMessage("You have get " + Config.CHAT_FILTER_PUNISHMENT_PARAM2 + " karma for bad words");
				}
				else if(Config.CHAT_FILTER_PUNISHMENT.equalsIgnoreCase("jail"))
				{
					activeChar.setPunishLevel(PunishLevel.JAIL, Config.CHAT_FILTER_PUNISHMENT_PARAM1);
				}	
				_text = filteredText;
			}
		}
	}
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}