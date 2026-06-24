package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.event.ctf.CTFEvent;
import net.sf.l2j.event.deathmath.DMEvent;
import net.sf.l2j.event.fortress.FOSEvent;
import net.sf.l2j.event.lastman.LMEvent;
import net.sf.l2j.event.tvt.TvTEvent;
import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.model.zone.type.L2TimeFarmZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.AskJoinParty;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.timezone.TimeFarmZoneManager;

/**
 * format cdd
 */
public final class RequestJoinParty extends L2GameClientPacket
{
	private String _name;
	private int _itemDistribution;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
		_itemDistribution = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player requestor = getClient().getActiveChar();
		if (requestor == null)
			return;
		
		final Player target = L2World.getInstance().getPlayer(_name);
		if (target == null)
		{
			requestor.sendPacket(SystemMessageId.FIRST_SELECT_USER_TO_INVITE_TO_PARTY);
			return;
		}
		
		if (BlockList.isBlocked(target, requestor))
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST).addPcName(target));
			return;
		}
		if (target.isPartyInRefuse())
		{
			requestor.sendMessage("The player you tried to invite is in refusal party mode.");
			return;
		}
		if (target.equals(requestor) || target.isCursedWeaponEquipped() || requestor.isCursedWeaponEquipped() || target.getAppearance().getInvisible())
		{
			requestor.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}
		
		if (LMEvent.isPlayerParticipant(target.getObjectId()) || LMEvent.isPlayerParticipant(requestor.getObjectId()))
		{
			requestor.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}
		for (L2TimeFarmZone zone : TimeFarmZoneManager.getZones())
		{
			if (!zone.isInvitePartyAllowed() && (zone.isInsideZone(requestor) || zone.isInsideZone(target)))
			{
				requestor.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
				return;
			}
		}
		
		if (TvTEvent.isPlayerParticipant(target.getObjectId()) || TvTEvent.isPlayerParticipant(requestor.getObjectId()))
		{
			requestor.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}
		if (CTFEvent.isPlayerParticipant(target.getObjectId()) || CTFEvent.isPlayerParticipant(requestor.getObjectId()))
		{
			requestor.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}
		
		if (DMEvent.isPlayerParticipant(target.getObjectId()) || DMEvent.isPlayerParticipant(requestor.getObjectId()))
		{
			requestor.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}
		if (FOSEvent.isPlayerParticipant(target.getObjectId()) || FOSEvent.isPlayerParticipant(requestor.getObjectId()))
		{
			requestor.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}
		if (Config.NO_INVITE_PVPEVENT && target.isInsideZone(ZoneId.PVP_CUSTOM) || requestor.isInsideZone(ZoneId.PVP_CUSTOM))
		{
			requestor.sendMessage("The player you tried to invite is in refusal party your PvP Event.");
			return;
		}
		if (target.isInParty())
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_ALREADY_IN_PARTY).addPcName(target));
			return;
		}
		
		if (target.getClient().isDetached())
		{
			requestor.sendMessage("The player you tried to invite is in offline mode.");
			return;
		}
		
		if (target.isInJail() || requestor.isInJail())
		{
			requestor.sendMessage("The player you tried to invite is currently jailed.");
			return;
		}
		
		if (target.isInOlympiadMode() || requestor.isInOlympiadMode())
			return;
		
		if (!requestor.isInParty())
			createNewParty(target, requestor);
		else
		{
			if (!requestor.getParty().isInDimensionalRift())
				addTargetToParty(target, requestor);
		}
	}
	
	/**
	 * @param target
	 * @param requestor
	 */
	private static void addTargetToParty(Player target, Player requestor)
	{
		final L2Party party = requestor.getParty();
		if (party == null)
			return;
		
		if (!party.isLeader(requestor))
		{
			requestor.sendPacket(SystemMessageId.ONLY_LEADER_CAN_INVITE);
			return;
		}
		
		if (party.getMemberCount() >= 9)
		{
			requestor.sendPacket(SystemMessageId.PARTY_FULL);
			return;
		}
		
		if (party.getPendingInvitation() && !party.isInvitationRequestExpired())
		{
			requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
			return;
		}
		
		if (!target.isProcessingRequest())
		{
			requestor.onTransactionRequest(target);
			
			// in case a leader change has happened, use party's mode
			target.sendPacket(new AskJoinParty(requestor.getName(), party.getLootDistribution()));
			party.setPendingInvitation(true);
			
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_INVITED_S1_TO_PARTY).addPcName(target));
			target.setPartyInvite(requestor, target);
			//
			if (Config.DEBUG)
				_log.fine("Sent out a party invitation to " + target.getName());
		}
		else
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addPcName(target));
			if (Config.DEBUG)
				_log.warning(requestor.getName() + " already received a party invitation");
		}
	}
	
	/**
	 * @param target
	 * @param requestor
	 */
	private void createNewParty(Player target, Player requestor)
	{
		if (!target.isProcessingRequest())
		{
			requestor.setParty(new L2Party(requestor, _itemDistribution));
			
			requestor.onTransactionRequest(target);
			target.sendPacket(new AskJoinParty(requestor.getName(), _itemDistribution));
			requestor.getParty().setPendingInvitation(true);
			
			if (Config.DEBUG)
				_log.fine("Sent out a party invitation to " + target.getName());
			
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_INVITED_S1_TO_PARTY).addPcName(target));
			target.setPartyInvite(requestor, target);
		}
		else
		{
			requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
			
			if (Config.DEBUG)
				_log.warning(requestor.getName() + " already received a party invitation");
		}
	}
}