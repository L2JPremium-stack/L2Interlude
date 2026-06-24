/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://eternity-world.ru/>.
 */
package net.sf.l2j.gameserver.model.actor.instance;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.model.L2AgathionAI;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.AbstractNpcInfo.NpcInfo;

public class L2AgathionInstance extends L2MonsterInstance
{
	protected static final Logger _logPet = Logger.getLogger(L2AgathionInstance.class.getName());

	private Player _owner;
	private boolean _isTargetable = true;

	private boolean _follow = true;
	
	public L2AgathionInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		
		setShowSummonAnimation(true);
	//	setAI(new L2AgathionAI(new AIAccessor()));
		setAI (new L2AgathionAI(this));
	}

	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		return true;
	}
	
	public synchronized void startAgathionTask()
	{
		
		if (!isDead()) // && (getOwner().getAgathion() == this))
		{
			return;
		}
	}
	
	public synchronized void unSummonAgathion(Player owner)
	{
		deleteMe();
	}

	@Override
	public void deleteMe()
	{
		stopHpMpRegeneration();

		setTarget(null);
		_owner = null;
		super.deleteMe();
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		this.setFollowStatus(true);
	}

	public void setFollowStatus(boolean state)
	{
		_follow = state;
		if (_follow)
		{
			getAI().setIntention(CtrlIntention.FOLLOW, getOwner());
		}
		else
		{
			getAI().setIntention(CtrlIntention.IDLE, null);
		}
	}

	public boolean getFollowStatus()
	{
		return _follow;
	}

	public void setTargetable(boolean b)
	{
		_isTargetable = b;
		broadcastStatusUpdate();
	}
	
	public void setOwner(Player owner)
	{
		_owner = owner;
	}

	@Override
	public boolean isTargetable()
	{
		return _isTargetable;
	}
	
	public Player getOwner()
	{
		return _owner;
	}

	@Override
	public boolean isAgathion()
	{
		return true;
	}
	
	public boolean useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		if ((skill == null) || isDead() || (getOwner() == null))
		{
			return false;
		}

		if (skill.isPassive())
		{
			return false;
		}

		if (isCastingNow())
		{
			return false;
		}
		getOwner().setCurrentPetSkill(skill, forceUse, dontMove);

		L2Object target = null;

		switch (skill.getTargetType())
		{
			case TARGET_OWNER_PET:
				target = getOwner();
				break;
			case TARGET_PARTY:
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_SELF:
			case TARGET_AREA_CORPSE_MOB:
				target = this;
				break;
			default:
				target = skill.getFirstOfTargetList(this);
				break;
		}

		if (target == null)
		{
			getOwner().sendPacket(SystemMessageId.TARGET_CANT_FOUND);
			return false;
		}

		if (isSkillDisabled(skill))
		{
			//sendMessage((new CustomMessage("PET_SKILL_CANNOT_BE_USED_RECHARCHING", getActingPlayer().getLang())).toString());
			return false;
		}

		if (getCurrentMp() < (getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill)))
		{
			getOwner().sendPacket(SystemMessageId.NOT_ENOUGH_MP);
			return false;
		}

		if (getCurrentHp() <= skill.getHpConsume())
		{
			getOwner().sendPacket(SystemMessageId.NOT_ENOUGH_HP);
			return false;
		}

		if (skill.isOffensive())
		{
			if (getOwner() == target)
			{
				return false;
			}

			if (isInsidePeaceZone(this, target) && !getOwner().getAccessLevel().allowPeaceAttack())
			{
				getOwner().sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
				return false;
			}

			if (getOwner().isInOlympiadMode() && !getOwner().isOlympiadStart())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			if ((target.getActingPlayer() != null) && (getOwner().getSiegeState() > 0) && getOwner().isInsideZone(ZoneId.SIEGE) && (target.getActingPlayer().getSiegeState() == getOwner().getSiegeState()) && (target.getActingPlayer() != getOwner()) && (target.getActingPlayer().getSiegeState() == getOwner().getSiegeState()))
			{
				//sendMessage((new CustomMessage("FORCED_ATTACK_IMPOSSIBLE_AT_SIEGE", getActingPlayer().getLang())).toString());
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			if (target instanceof L2DoorInstance)
			{
				if (!target.isAutoAttackable(getOwner()))
				{
					return false;
				}
			}
			else
			{
				if (!target.isAttackable() && !getOwner().getAccessLevel().allowPeaceAttack())
				{
					return false;
				}

				if (!target.isAutoAttackable(this) && !forceUse /*&& !target.isNpc()*/ && (skill.getTargetType() != SkillTargetType.TARGET_AURA) && (skill.getTargetType() != SkillTargetType.TARGET_FRONT_AURA) && (skill.getTargetType() != SkillTargetType.TARGET_BEHIND_AURA) && (skill.getTargetType() != SkillTargetType.TARGET_CLAN) && (skill.getTargetType() != SkillTargetType.TARGET_PARTY) && (skill.getTargetType() != SkillTargetType.TARGET_SELF))
				{
					return false;
				}
			}
		}
		getAI().setIntention(CtrlIntention.CAST, skill, target);
		return true;
	}

	public void broadcastNpcInfo(int val)
	{
		for (Player player : getKnownList().getKnownType(Player.class))
		{
			if ((player == null) || (player == getOwner()))
			{
				continue;
			}
			player.sendPacket(new NpcInfo(this, player));
		}
	}
}