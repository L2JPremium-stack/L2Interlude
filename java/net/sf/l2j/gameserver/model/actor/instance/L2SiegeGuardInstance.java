package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.model.L2CharacterAI;
import net.sf.l2j.gameserver.ai.model.L2SiegeGuardAI;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.knownlist.SiegeGuardKnownList;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;

/**
 * This class represents all guards in the world.
 */
public final class L2SiegeGuardInstance extends Attackable
{
	public L2SiegeGuardInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new SiegeGuardKnownList(this));
	}
	
	@Override
	public final SiegeGuardKnownList getKnownList()
	{
		return (SiegeGuardKnownList) super.getKnownList();
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai;
		if (ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
					_ai = new L2SiegeGuardAI(this);
				
				return _ai;
			}
		}
		return ai;
	}
	
	/**
	 * Return True if a siege is in progress and the L2Character attacker isn't a Defender.
	 * @param attacker The L2Character that the L2SiegeGuardInstance try to attack
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		// Attackable during siege by all except defenders
		return (attacker != null && attacker.getActingPlayer() != null && getCastle() != null && getCastle().getSiege().isInProgress() && !getCastle().getSiege().checkIsDefender(attacker.getActingPlayer().getClan()));
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	/**
	 * Note that super() is not called because guards need extra check to see if a player should interact or ATTACK them when clicked.
	 */
	@Override
	public void onAction(Player player)
	{
		// Set the target of the L2PcInstance player
		if (player.getTarget() != this)
		{
			player.setTarget(this);	
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			if (isAutoAttackable(player))
			{
				if (!isAlikeDead() && (Math.abs(player.getZ() - getZ()) < 600)) // this max heigth difference might need some tweaking
					player.getAI().setIntention(CtrlIntention.ATTACK, this);
			}
			else
			{
				// Notify the L2PcInstance AI with INTERACT
				if (!canInteract(player))
					player.getAI().setIntention(CtrlIntention.INTERACT, this);
				else
				{
					// Rotate the player to face the instance
					player.sendPacket(new MoveToPawn(player, this, L2Npc.INTERACTION_DISTANCE));
					
					// Send ActionFailed to the player in order to avoid he stucks
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
		}
	}
	
	@Override
	public void addDamageHate(Creature attacker, int damage, int aggro)
	{
		if (attacker == null)
			return;
		
		if (!(attacker instanceof L2SiegeGuardInstance))
			super.addDamageHate(attacker, damage, aggro);
	}
	
	@Override
	public boolean isGuard()
	{
		return true;
	}
	
	@Override
	public int getDriftRange()
	{
		return 20;
	}
}