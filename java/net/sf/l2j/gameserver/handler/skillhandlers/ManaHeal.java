package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.ShotType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.skills.L2SkillType;

public class ManaHeal implements ISkillHandler
{
    private static final L2SkillType[] SKILL_IDS =
    {
        L2SkillType.MANAHEAL,
        L2SkillType.MANARECHARGE
    };

    @Override
    public void useSkill(Creature activeChar, L2Skill skill, L2Object[] targets)
    {
        for (L2Object obj : targets)
        {
            if (!(obj instanceof Creature))
                continue;

            final Creature target = (Creature) obj;
            if (target.isInvul())
                continue;

            double mp = skill.getPower();

            if (skill.getSkillType() == L2SkillType.MANAHEAL_PERCENT)
                mp = target.getMaxMp() * mp / 100.0;
            else if (skill.getSkillType() == L2SkillType.MANARECHARGE)
                mp = target.calcStat(Stats.RECHARGE_MP_RATE, mp, null, null);

            // ----- Penalidade por diferença de níveis entre caster e alvo -----
            if (target.getLevel() > activeChar.getLevel())
            {
                int lvlDiff = target.getLevel() - activeChar.getLevel();

                if (lvlDiff == 6)       mp *= 0.9;
                else if (lvlDiff == 7)  mp *= 0.8;
                else if (lvlDiff == 8)  mp *= 0.7;
                else if (lvlDiff == 9)  mp *= 0.6;
                else if (lvlDiff == 10) mp *= 0.5;
                else if (lvlDiff == 11) mp *= 0.4;
                else if (lvlDiff == 12) mp *= 0.3;
                else if (lvlDiff == 13) mp *= 0.2;
                else if (lvlDiff == 14) mp *= 0.1;
                else if (lvlDiff >= 15) mp = 0;
            }
            // -----------------------------------------------------------------

            // Não ultrapassar MP máximo
            mp = Math.min(mp, target.getMaxMp() - target.getCurrentMp());
            if (mp < 0)
                mp = 0;

            target.setCurrentMp(target.getCurrentMp() + mp);
            StatusUpdate sump = new StatusUpdate(target);
            sump.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
            target.sendPacket(sump);

            if (activeChar instanceof Player && activeChar != target)
                target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_MP_RESTORED_BY_S1)
                        .addCharName(activeChar).addNumber((int) mp));
            else
                target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED).addNumber((int) mp));
        }

        if (skill.hasSelfEffects())
        {
            final L2Effect effect = activeChar.getFirstEffect(skill.getId());
            if (effect != null && effect.isSelfEffect())
                effect.exit();

            skill.getEffectsSelf(activeChar);
        }

        if (!skill.isPotion())
            activeChar.setChargedShot(
                    activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT) ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT,
                    skill.isStaticReuse());
    }

    @Override
    public L2SkillType[] getSkillIds()
    {
        return SKILL_IDS;
    }
}
