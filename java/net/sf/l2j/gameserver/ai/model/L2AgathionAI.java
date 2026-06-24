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
package net.sf.l2j.gameserver.ai.model;

import java.util.concurrent.Future;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.L2AgathionInstance;

public class L2AgathionAI extends L2CharacterAI implements Runnable
{
	private static final int AVOID_RADIUS = 70;
	
	private volatile boolean _thinking;
	private volatile boolean _startFollow = ((L2AgathionInstance) _actor).getFollowStatus();
	private Creature _lastAttack = null;
	
	private volatile boolean _startAvoid = false;
	private Future<?> _avoidTask = null;
	
	public L2AgathionAI(L2AgathionInstance accessor)
	{
		super(accessor);
	}
	
	@Override
	protected void onIntentionIdle()
	{
		_actor.getAI().stopFollow();
		_startFollow = false;
		onIntentionActive();
	}
	
	@Override
	protected void onIntentionActive()
	{
		L2AgathionInstance agathion = (L2AgathionInstance) _actor;
		if (_startFollow)
		{
			setIntention(CtrlIntention.FOLLOW, agathion.getOwner());
		}
		else
		{
			super.onIntentionActive();
		}
	}
	
	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		switch (intention)
		{
			case ACTIVE:
			case FOLLOW:
				startAvoidTask();
				break;
			default:
				stopAvoidTask();
		}
		
		super.changeIntention(intention, arg0, arg1);
	}
	
	private void thinkCast()
	{
		L2AgathionInstance agathion = (L2AgathionInstance) _actor;
		if (checkTargetLost(getTarget()))
		{
			setTarget(null);
			return;
		}
		boolean val = _startFollow;
		if (_actor.getAI().maybeMoveToPawn(getTarget(), _skill.getCastRange()))
		{
			return;
		}
		//clientStopMoving(null);
		agathion.setFollowStatus(false);
		setIntention(CtrlIntention.IDLE);
		_startFollow = val;
		_actor.doCast(_skill);
	}
	
	private void thinkInteract()
	{
		if (checkTargetLost(getTarget()))
		{
			return;
		}
		if (_actor.getAI().maybeMoveToPawn(getTarget(), 36))
		{
			return;
		}
		setIntention(CtrlIntention.IDLE);
	}
	
	@Override
	protected void onEvtThink()
	{
		if (_thinking || _actor.isCastingNow() || _actor.isAllSkillsDisabled())
		{
			return;
		}
		_thinking = true;
		try
		{
			switch (getIntention())
			{
				case CAST:
					thinkCast();
					break;
				case INTERACT:
					thinkInteract();
					break;
			}
		}
		finally
		{
			_thinking = false;
		}
	}
	
	@Override
	protected void onEvtFinishCasting()
	{
		if (_lastAttack == null)
		{
			((L2AgathionInstance) _actor).setFollowStatus(_startFollow);
		}
		else
		{
			setIntention(CtrlIntention.ATTACK, _lastAttack);
			_lastAttack = null;
		}
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker)
	{
		super.onEvtAttacked(attacker);
		
		avoidAttack(attacker);
	}
	
	@Override
	protected void onEvtEvaded(Creature attacker)
	{
		super.onEvtEvaded(attacker);
		
		avoidAttack(attacker);
	}
	
	private void avoidAttack(Creature attacker)
	{
		if ((((L2AgathionInstance) _actor).getOwner() != null) && (((L2AgathionInstance) _actor).getOwner() != attacker) && ((L2AgathionInstance) _actor).getOwner().isInsideRadius(_actor, 2 * AVOID_RADIUS, true, false))
		{
			_startAvoid = true;
		}
	}
	
	
	
	
	public void notifyFollowStatusChange()
	{
		_startFollow = !_startFollow;
		switch (getIntention())
		{
			case ACTIVE:
			case FOLLOW:
			case IDLE:
			case MOVE_TO:
				((L2AgathionInstance) _actor).setFollowStatus(_startFollow);
		}
	}
	@Override
	public void run()
	{
		if (_startAvoid)
		{
			_startAvoid = false;
			
			if (!_actor.isMoving() && !_actor.isDead() && !_actor.isMovementDisabled())
			{
			    final int ownerX = ((L2AgathionInstance) _actor).getOwner().getX();
			    final int ownerY = ((L2AgathionInstance) _actor).getOwner().getY();

			    // Restringe o ângulo para que o agathion fique apenas ao lado do jogador (-90 a 90 graus do lado)
			    final double angle = Math.toRadians(Rnd.get(60, 120)) * (Rnd.nextBoolean() ? 1 : -1); // Apenas lados (esquerdo ou direito)

			    // Calcula as coordenadas de destino
			    final int targetX = ownerX + (int) (AVOID_RADIUS * Math.cos(angle));
			    final int targetY = ownerY + (int) (AVOID_RADIUS * Math.sin(angle));

			    // Verifica se é possível mover para o local calculado
			    if (GeoEngine.getInstance().canMoveToTarget(_actor.getX(), _actor.getY(), _actor.getZ(), targetX, targetY, _actor.getZ()))
			    {
			        _actor.moveToLocation(targetX, targetY, _actor.getZ(), 0);
			    }

			}

		}
	}
	public void setStartFollowController(boolean val)
	{
		_startFollow = val;
	}
	
	@Override
	protected void onIntentionCast(L2Skill skill, L2Object target)
	{
		if (getIntention() == CtrlIntention.ATTACK)
		{
			_lastAttack = (Creature) getTarget();
		}
		else
		{
			_lastAttack = null;
		}
		super.onIntentionCast(skill, target);
	}
	
	private void startAvoidTask()
	{
		if (_avoidTask == null)
		{
			_avoidTask = ThreadPool.scheduleAtFixedRate(this, 100, 100);
		}
	}
	
	private void stopAvoidTask()
	{
		if (_avoidTask != null)
		{
			_avoidTask.cancel(false);
			_avoidTask = null;
		}
	}
	
	@Override
	public void stopAITask()
	{
		stopAvoidTask();
		super.stopAITask();
	}
}