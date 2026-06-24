package net.sf.l2j.sellbuff;

import net.sf.l2j.Config;
import net.sf.l2j.event.bossevent.KTBEvent;
import net.sf.l2j.event.ctf.CTFEvent;
import net.sf.l2j.event.deathmath.DMEvent;
import net.sf.l2j.event.fortress.FOSEvent;
import net.sf.l2j.event.lastman.LMEvent;
import net.sf.l2j.event.tvt.TvTEvent;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.model.zone.ZoneId;

public class SellBuffsCommand implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"sellbuffs"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (activeChar == null)
			return false;
		
		if (activeChar.isDead() || activeChar.isAlikeDead())
		{
			activeChar.sendMessage("You are dead , you can't sell at the moment");
			return false;
		}
		else if (activeChar.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(activeChar))
		{
			activeChar.sendMessage("You are in olympiad , you can't sell at the moment");
			return false;
		}
		else if (activeChar.isArenaProtection() || activeChar.isArenaAttack() || TvTEvent.isPlayerParticipant(activeChar.getObjectId()) || CTFEvent.isPlayerParticipant(activeChar.getObjectId()) || LMEvent.isPlayerParticipant(activeChar.getObjectId()) || DMEvent.isPlayerParticipant(activeChar.getObjectId()) || KTBEvent.isPlayerParticipant(activeChar.getObjectId()) || FOSEvent.isPlayerParticipant(activeChar.getObjectId()))
		{
			activeChar.sendMessage("You are registed in events");
			return false;
		}
		else if (!activeChar.isInsideZone(ZoneId.PEACE))
		{
			activeChar.sendMessage("You are not in peacefull zone , you can sell only in peacefull zones");
			return false;
		}
		else if (activeChar.isSubClassActive() && Config.BLOCK_SELLBUFF_SUBCLASS)
		{
			activeChar.sendMessage("Impossible Sell Buff SubClass Characters.");
			return false;
		}
		else if (!Config.SELL_BUFF_CLASS_LIST.contains(Integer.toString(activeChar.getClassId().getId())))
		{
			activeChar.sendMessage("Your class can't sell buffs");
			return false;
		}
		else if (activeChar.getLevel() < Config.SELL_BUFF_MIN_LVL)
		{
			activeChar.sendMessage("You can sell buffs on " + Config.SELL_BUFF_MIN_LVL + " level");
			return false;
		}
		// summoner classes exception, buffs allowed from 56 level.
		else if (activeChar.getClassId().getId() == 96 || activeChar.getClassId().getId() == 14 || activeChar.getClassId().getId() == 104 || activeChar.getClassId().getId() == 28)
		{
			if (activeChar.getLevel() < 56)
			{
				activeChar.sendMessage("You can sell buffs on 56 level");
				return false;
			}
		}
		
		activeChar.getSellBuffMsg().sendSellerResponse(activeChar);
		activeChar.broadcastUserInfo();
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
}