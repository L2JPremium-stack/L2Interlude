package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.ShotType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.L2Summon;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeFlagInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.skills.L2SkillType;

public class Heal implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.HEAL,
		L2SkillType.HEAL_STATIC,
		L2SkillType.HEAL_PERCENT
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, L2Object[] targets)
	{
		// Aplica buffs se houver
		final ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(L2SkillType.BUFF);
		if (handler != null)
			handler.useSkill(activeChar, skill, targets);
		
		double power = skill.getPower() + activeChar.calcStat(Stats.HEAL_PROFICIENCY, 0, null, null);
		
		final boolean sps = activeChar.isChargedShot(ShotType.SPIRITSHOT);
		final boolean bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);
		
		switch (skill.getSkillType())
		{
			case HEAL_STATIC:
				break;
			
			default:
				double staticShotBonus = 0;
				int mAtkMul = 1;
				
				if ((sps || bsps) && ((activeChar instanceof Player && activeChar.getActingPlayer().isMageClass()) || activeChar instanceof L2Summon))
				{
					staticShotBonus = skill.getMpConsume();
					if (bsps)
					{
						mAtkMul = 4;
						staticShotBonus *= 2.4;
					}
					else
						mAtkMul = 2;
				}
				else if ((sps || bsps) && activeChar instanceof L2Summon)
				{
					staticShotBonus = 2.4 * skill.getMpConsume();
					mAtkMul = 4;
				}
				else
				{
					if (bsps)
						mAtkMul *= 4;
					else
						mAtkMul += 1;
				}
				
				power += staticShotBonus + Math.sqrt(mAtkMul * activeChar.getMAtk(activeChar, null));
				
				if (!skill.isPotion())
					activeChar.setChargedShot(bsps ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, skill.isStaticReuse());
		}
		
		for (L2Object obj : targets)
		{
			if (!(obj instanceof Creature))
				continue;
			
			Creature target = (Creature) obj;
			
			if (target.isDead() || target.isInvul() || target instanceof L2DoorInstance || target instanceof L2SiegeFlagInstance)
				continue;
			
			if (!Config.PLAYERS_CAN_HEAL_RB && activeChar instanceof Player && (target instanceof L2RaidBossInstance || target instanceof L2GrandBossInstance) && (skill.getSkillType() == L2SkillType.HEAL || skill.getSkillType() == L2SkillType.HEAL_PERCENT))
				return;
			
			// Cursed weapon checks
			if (target != activeChar)
			{
				if (target instanceof Player && ((Player) target).isCursedWeaponEquipped())
					continue;
				else if (activeChar instanceof Player && ((Player) activeChar).isCursedWeaponEquipped())
					continue;
			}
			
			double hp;
			
			if (skill.getSkillType() == L2SkillType.HEAL_PERCENT)
				hp = target.getMaxHp() * power / 100.0;
			else
				hp = power;
			
			// APLICA MULTIPLICADOR DE CURA PARA PETS ANTES DE HEAL_EFFECTIVENESS
			if (activeChar instanceof L2Summon)
			{
				double mult = Config.PET_HEAL_POWER_MULTIPLIER;
				if (Config.ENABLE_CUSTOM_PET_MULTIPLIERS)
				{
					double npcMult = Config.CUSTOM_PET_MULTIPLIERS.getOrDefault(((L2Summon) activeChar).getNpcId(), 1.0);
					mult *= npcMult;
				}
				hp *= mult;
			}
			
			// Aplica HEAL_EFFECTIVENESS do target
			hp *= target.calcStat(Stats.HEAL_EFFECTIVNESS, 100, null, null) / 100.0;
			
			if ((target.getCurrentHp() + hp) > target.getMaxHp())
				hp = target.getMaxHp() - target.getCurrentHp();
			
			if (hp < 0)
				hp = 0;
			
			target.setCurrentHp(target.getCurrentHp() + hp);
			StatusUpdate su = new StatusUpdate(target);
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			target.sendPacket(su);
			
			if (target instanceof Player)
			{
				if (skill.getId() == 4051)
					target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REJUVENATING_HP));
				else
				{
					if (activeChar instanceof Player && activeChar != target)
						target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_S1).addCharName(activeChar).addNumber((int) hp));
					else
						target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED).addNumber((int) hp));
				}
			}
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
