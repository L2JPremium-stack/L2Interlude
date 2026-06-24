package net.sf.l2j.gameserver.model.zone.type;

import java.util.concurrent.Future;

import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;

/**
 * A dynamic zone? Maybe use this for interlude skills like protection field :>
 * @author durgus
 */
public class L2DynamicZone extends L2ZoneType
{
	private final L2WorldRegion _region;
	private final Creature _owner;
	private Future<?> _task;
	private final L2Skill _skill;
	
	protected void setTask(Future<?> task)
	{
		_task = task;
	}
	
	public L2DynamicZone(L2WorldRegion region, Creature owner, L2Skill skill)
	{
		super(-1);
		_region = region;
		_owner = owner;
		_skill = skill;
		
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				remove();
			}
		};
		setTask(ThreadPool.schedule(r, skill.getBuffDuration()));
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		try
		{
			if (character instanceof Player)
				((Player) character).sendMessage("You have entered a temporary zone!");
			_skill.getEffects(_owner, character);
		}
		catch (NullPointerException e)
		{
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		if (character instanceof Player)
			((Player) character).sendMessage("You have left a temporary zone!");
		
		if (character == _owner)
		{
			remove();
			return;
		}
		character.stopSkillEffects(_skill.getId());
	}
	
	protected void remove()
	{
		if (_task == null)
			return;
		
		_task.cancel(false);
		_task = null;
		
		_region.removeZone(this);
		for (Creature member : _characterList)
		{
			try
			{
				member.stopSkillEffects(_skill.getId());
			}
			catch (NullPointerException e)
			{
			}
		}
		_owner.stopSkillEffects(_skill.getId());
		
	}
	
	@Override
	public void onDieInside(Creature character)
	{
		if (character == _owner)
			remove();
		else
			character.stopSkillEffects(_skill.getId());
	}
	
	@Override
	public void onReviveInside(Creature character)
	{
		_skill.getEffects(_owner, character);
	}
}