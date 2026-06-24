package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.event.bossevent.KTBEvent;
import net.sf.l2j.event.ctf.CTFEvent;
import net.sf.l2j.event.deathmath.DMEvent;
import net.sf.l2j.event.fortress.FOSEvent;
import net.sf.l2j.event.tvt.TvTEvent;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

public class ScrollOfResurrection implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
				
		if (!KTBEvent.onScrollUse(playable.getObjectId()))
		{
			playable.sendMessage("You can not do this in KTB Event");
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!TvTEvent.onScrollUse(playable.getObjectId()))
		{
			playable.sendMessage("You can not do this in TvT Event");
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!CTFEvent.onScrollUse(playable.getObjectId()))
		{
			playable.sendMessage("You can not do this in CTF Event");
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!DMEvent.onScrollUse(playable.getObjectId()))
		{
			playable.sendMessage("You can not do this in DM Event");
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!FOSEvent.onScrollUse(playable.getObjectId()))
		{
			playable.sendMessage("You can not do this in FOS Event");
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		final Player activeChar = (Player) playable;
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return;
		}
		
		if (activeChar.isMovementDisabled())
			return;
		
		final Creature target = (Creature) activeChar.getTarget();
		
		// Target must be a dead L2PetInstance or L2PcInstance.
		if ((!(target instanceof L2PetInstance) && !(target instanceof Player)) || !target.isDead())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		// Pet scrolls to ress a player.
		if (item.getItemId() == 6387 && target instanceof Player)
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		// Pickup player, or pet owner in case target is a pet.
		final Player targetPlayer = target.getActingPlayer();
		
		// Check if target isn't in a active siege zone.
		final Castle castle = CastleManager.getInstance().getCastle(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ());
		if (castle != null && castle.getSiege().isInProgress())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
			return;
		}
		
		// Check if the target is in a festival.
		if (targetPlayer.isFestivalParticipant())
			return;
		
		if (targetPlayer.isReviveRequested())
		{
			if (targetPlayer.isRevivingPet())
				activeChar.sendPacket(SystemMessageId.MASTER_CANNOT_RES); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
			else
				activeChar.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
				
			return;
		}
		
		final IntIntHolder[] skills = item.getEtcItem().getSkills();
		if (skills == null)
		{
			_log.info(item.getName() + " does not have registered any skill for handler.");
			return;
		}
		
		for (IntIntHolder skillInfo : skills)
		{
			if (skillInfo == null)
				continue;
			
			final L2Skill itemSkill = skillInfo.getSkill();
			if (itemSkill == null)
				continue;
			
			// Scroll consumption is made on skill call, not on item call.
			playable.useMagic(itemSkill, false, false);
		}
	}
}