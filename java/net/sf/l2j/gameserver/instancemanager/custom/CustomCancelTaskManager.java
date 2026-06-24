package net.sf.l2j.gameserver.instancemanager.custom;

import java.util.HashMap;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.effects.EffectTemplate;

public class CustomCancelTaskManager implements Runnable
{
	private Player _player = null;
	private HashMap<L2Skill, int[]> _buffs = null;

	public CustomCancelTaskManager(Player _player, HashMap<L2Skill, int[]> _buffs)
	{
		this._player = _player;
		this._buffs = _buffs;
	}

	@Override
	public void run()
	{
		if (_player == null || !_player.isOnline())
			return;

		for (L2Skill s : _buffs.keySet())
		{
			if (s == null)
				continue;

			Env env = new Env();
			env._character = _player;
			env._target = _player;
			env._skill = s;
			L2Effect ef;
			for (EffectTemplate et : s.getEffectTemplates())
			{
				ef = et.getEffect(env);
				if (ef != null)
				{
					ef.setCount(_buffs.get(s)[0]);
					ef.setFirstTime(_buffs.get(s)[1]);
					ef.scheduleEffect();
				}
			}
		}
	}
}