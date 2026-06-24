package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.L2ChestInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GuardInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

public final class AttackRequest extends L2GameClientPacket
{
	// cddddc
	private int _objectId;
	@SuppressWarnings("unused")
	private int _originX, _originY, _originZ;
	@SuppressWarnings("unused")
	private int _attackId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_attackId = readC(); // 0 for simple click 1 for shift-click
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (activeChar.inObserverMode())
		{
			activeChar.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// avoid using expensive operations if not needed
		final L2Object target;
		if (activeChar.getTargetId() == _objectId)
			target = activeChar.getTarget();
		else
			target = L2World.getInstance().getObject(_objectId);
		
		if (target == null)
			return;
		
		boolean target_alloewd = (!(target instanceof L2GuardInstance) && !(target instanceof L2MonsterInstance) && !(target instanceof L2RaidBossInstance) && !(target instanceof L2GrandBossInstance) && !(target instanceof L2DoorInstance) && !(target instanceof L2ChestInstance));
		
		if (!(target instanceof Player || target instanceof L2PetInstance || target instanceof L2SummonInstance) && target_alloewd && Config.DISABLE_ATTACK_NPC_TYPE)
		{
			activeChar.sendMessage("Attack Npcs Is Disabled!");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!target.isTargetable())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if(Config.ZONEPVPEVENT_ALLOW_INTERFERENCE)
		{
			if (target instanceof Player && !activeChar.isInsideZone(ZoneId.PVP_CUSTOM) && ((Player) target).isInsideZone(ZoneId.PVP_CUSTOM) || target instanceof Player && activeChar.isInsideZone(ZoneId.PVP_CUSTOM) && !((Player) target).isInsideZone(ZoneId.PVP_CUSTOM))
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
				
			}
		}
		//FIX Players fora da arena nao atacar jogadores dentro da arena, virse e versa
		if(!activeChar.isInsideZone(ZoneId.PEACE))
		{
			if (target instanceof Player && !activeChar.isInsideZone(ZoneId.PVP) && ((Player) target).isInsideZone(ZoneId.PVP) || target instanceof Player && activeChar.isInsideZone(ZoneId.PVP) && !((Player) target).isInsideZone(ZoneId.PVP))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				activeChar.sendMessage("Impossible Used Skills or Attacks in Players PvP");
				return;
				
			}	
		}	
		if (activeChar.getTarget() != target)
			target.onAction(activeChar);
		else
		{
			if ((target.getObjectId() != activeChar.getObjectId()) && !activeChar.isInStoreMode() && activeChar.getActiveRequester() == null)
				target.onForcedAttack(activeChar);
			else
				sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
}