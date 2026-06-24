package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.entity.Siegable;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class L2SiegeFlagInstance extends L2Npc
{
	private final L2Clan _clan;
	private final Player _player;
	private final Siegable _siege;
	
	public L2SiegeFlagInstance(Player player, int objectId, NpcTemplate template)
	{
		super(objectId, template);
		
		_player = player;
		_clan = player.getClan();
		_siege = SiegeManager.getSiege(_player.getX(), _player.getY(), _player.getZ());
		
		if (_clan == null || _siege == null)
			throw new NullPointerException(getClass().getSimpleName() + ": Initialization failed.");
		
		L2SiegeClan sc = _siege.getAttackerClan(_clan);
		if (sc == null)
			throw new NullPointerException(getClass().getSimpleName() + ": Cannot find siege clan.");
		
		sc.addFlag(this);
		setIsInvul(false);
		setScriptValue(1);
	}
	
	@Override
	public boolean isAttackable()
	{
		return !isInvul();
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return !isInvul();
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
			return false;
		
		if (_siege != null && _clan != null)
		{
			L2SiegeClan sc = _siege.getAttackerClan(_clan);
			if (sc != null)
				sc.removeFlag(this);
		}
		return true;
	}
	
	@Override
	public void onForcedAttack(Player player)
	{
		onAction(player);
	}
	
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
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100)
				player.getAI().setIntention(CtrlIntention.ATTACK, this);
			else
			{
				// Rotate the player to face the instance
				player.sendPacket(new MoveToPawn(player, this, L2Npc.INTERACTION_DISTANCE));
				
				// Send ActionFailed to the player in order to avoid he stucks
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, L2Skill skill)
	{
		// Send warning to owners of headquarters that theirs base is under attack.
		if (isScriptValue(1) && _clan != null && getCastle() != null && getCastle().getSiege().isInProgress())
		{
			_clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.BASE_UNDER_ATTACK));
			setScriptValue(0);
			ThreadPool.schedule(new ScheduleTalkTask(), 20000);
		}
		super.reduceCurrentHp(damage, attacker, skill);
	}
	
	private class ScheduleTalkTask implements Runnable
	{
		public ScheduleTalkTask()
		{
		}
		
		@Override
		public void run()
		{
			setScriptValue(1);
		}
	}
	
	@Override
	public void addFuncsToNewCharacter()
	{
	}
}