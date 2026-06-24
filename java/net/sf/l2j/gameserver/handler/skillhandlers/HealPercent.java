package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeFlagInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.skills.L2SkillType;

public class HealPercent implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.HEAL_PERCENT,
		L2SkillType.MANAHEAL_PERCENT,
		L2SkillType.CPHEAL_PERCENT
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, L2Object[] targets)
	{
		// check for other effects
		ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(L2SkillType.BUFF);
		if (handler != null)
			handler.useSkill(activeChar, skill, targets);
		
		boolean cp = false;
		boolean hp = false;
		boolean mp = false;
		
		switch (skill.getSkillType())
		{
			case CPHEAL_PERCENT:
				cp = true;
				break;
			
			case HEAL_PERCENT:
				hp = true;
				break;
			
			case MANAHEAL_PERCENT:
				mp = true;
				break;
		}
		
		StatusUpdate su = null;
		SystemMessage sm;
		double amount = 0;
		boolean full = skill.getPower() == 100.0;
		boolean targetPlayer = false;
		
		for (L2Object obj : targets)
		{
			if (!(obj instanceof Creature))
				continue;
			
			final Creature target = ((Creature) obj);
			if (target.isDead() || target.isInvul())
				continue;
			
			if (!Config.PLAYERS_CAN_HEAL_RB && activeChar instanceof Player && (target instanceof L2RaidBossInstance || target instanceof L2GrandBossInstance) && (skill.getSkillType() == L2SkillType.HEAL || skill.getSkillType() == L2SkillType.HEAL_PERCENT))
			{
				 return;
			}
			
			// Doors and flags can't be healed in any way
			if (target instanceof L2DoorInstance || target instanceof L2SiegeFlagInstance)
				continue;
			
			targetPlayer = target instanceof Player;
			
			// Cursed weapon owner can't heal or be healed
			if (target != activeChar)
			{
				if (activeChar instanceof Player && ((Player) activeChar).isCursedWeaponEquipped())
					continue;
				
				if (targetPlayer && ((Player) target).isCursedWeaponEquipped())
					continue;
			}
			
			if (hp)
			{
				amount = Math.min(((full) ? target.getMaxHp() : target.getMaxHp() * skill.getPower() / 100.0), target.getMaxHp() - target.getCurrentHp());
				target.setCurrentHp(amount + target.getCurrentHp());
			}
			else if (mp)
			{
				amount = Math.min(((full) ? target.getMaxMp() : target.getMaxMp() * skill.getPower() / 100.0), target.getMaxMp() - target.getCurrentMp());
				target.setCurrentMp(amount + target.getCurrentMp());
			}
			
			if (targetPlayer)
			{
				su = new StatusUpdate(target);
				
				if (cp)
				{
					amount = Math.min(((full) ? target.getMaxCp() : (target.getMaxCp() * skill.getPower() / 100.0)), target.getMaxCp() - target.getCurrentCp());
					target.setCurrentCp(amount + target.getCurrentCp());
					
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED);
					sm.addNumber((int) amount);
					target.sendPacket(sm);
					su.addAttribute(StatusUpdate.CUR_CP, (int) target.getCurrentCp());
				}
				else if (hp)
				{
					if (activeChar != target)
						sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_S1).addCharName(activeChar);
					else
						sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
					
					sm.addNumber((int) amount);
					target.sendPacket(sm);
					su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
				}
				else if (mp)
				{
					if (activeChar != target)
						sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MP_RESTORED_BY_S1).addCharName(activeChar);
					else
						sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED);
					
					sm.addNumber((int) amount);
					target.sendPacket(sm);
					su.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
				}
				
				target.sendPacket(su);
			}
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}