package net.sf.l2j.gameserver.model;

import java.util.concurrent.Future;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.skills.effects.EffectFusion;
import net.sf.l2j.gameserver.util.Util;

/**
 * @author kombat, Forsaiken
 */
public final class FusionSkill
{
	protected static final Logger _log = Logger.getLogger(FusionSkill.class.getName());
	
	protected int _skillCastRange;
	protected int _fusionId;
	protected int _fusionLevel;
	protected Creature _caster;
	protected Creature _target;
	protected Future<?> _geoCheckTask;
	
	public Creature getCaster()
	{
		return _caster;
	}
	
	public Creature getTarget()
	{
		return _target;
	}
	
	public FusionSkill(Creature caster, Creature target, L2Skill skill)
	{
		_skillCastRange = skill.getCastRange();
		_caster = caster;
		_target = target;
		_fusionId = skill.getTriggeredId();
		_fusionLevel = skill.getTriggeredLevel();
		
		L2Effect effect = _target.getFirstEffect(_fusionId);
		if (effect != null)
			((EffectFusion) effect).increaseEffect();
		else
		{
			L2Skill force = SkillTable.getInstance().getInfo(_fusionId, _fusionLevel);
			if (force != null)
				force.getEffects(_caster, _target, null);
			else
				_log.warning("Triggered skill [" + _fusionId + ";" + _fusionLevel + "] not found!");
		}
		_geoCheckTask = ThreadPool.scheduleAtFixedRate(new GeoCheckTask(), 1000, 1000);
	}
	
	public void onCastAbort()
	{
		final Creature caster = _caster;
		final Creature target = _target;
		
		if (caster != null)
			caster.setFusionSkill(null);
		
		if (target != null)
		{
			L2Effect effect = target.getFirstEffect(_fusionId);
			if (effect != null)
				((EffectFusion) effect).decreaseForce();
		}
		
		if (_geoCheckTask != null)
		{
			_geoCheckTask.cancel(true);
			_geoCheckTask = null;
		}
		
		_caster = null;
		_target = null;
	}
	
	public class GeoCheckTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				final Creature caster = _caster;
				final Creature target = _target;
				
				if (caster == null || target == null || caster.isAlikeDead() || target.isAlikeDead())
				{
					onCastAbort();
					return;
				}
				
				if (!Util.checkIfInRange(_skillCastRange, caster, target, true))
					caster.abortCast();
				
				if (!GeoEngine.getInstance().canSeeTarget(caster, target))
					caster.abortCast();
			}
			catch (Exception e)
			{
				onCastAbort();
			}
		}
	}
}