package net.sf.l2j.dailyreward;

import java.util.concurrent.ScheduledFuture;

import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.model.actor.Player;

public class PlayerVar
{
	private Player owner;
	private String name;
	private String value;
	private long expire_time;

	@SuppressWarnings("rawtypes")
	private ScheduledFuture task;

	public PlayerVar(Player owner, String name, String value, long expire_time)
	{
		this.owner = owner;
		this.name = name;
		this.value = value;
		this.expire_time = expire_time;

		if (expire_time > 0)
		{
			final long delay = expire_time - System.currentTimeMillis();
			if (delay > 0)
				task = ThreadPool.schedule(new PlayerVarExpireTask(this), delay);
			else
				task = ThreadPool.schedule(new PlayerVarExpireTask(this), 1);
		}
	}

	public String getName()
	{
		return name;
	}

	public Player getOwner()
	{
		return owner;
	}

	public boolean hasExpired()
	{
		return expire_time > 0 && System.currentTimeMillis() >= expire_time;
	}

	public long getTimeToExpire()
	{
		return expire_time - System.currentTimeMillis();
	}

	public String getValue()
	{
		return value;
	}

	public boolean getValueBoolean()
	{
		if (value == null)
			return false;

		if (isNumeric(value))
			return Integer.parseInt(value) > 0;

		return value.equalsIgnoreCase("true");
	}

	public void setValue(String val)
	{
		value = val;
	}

	public void stopExpireTask()
	{
		if (task != null)
		{
			task.cancel(false);
			task = null;
		}
	}

	public void cleanup()
	{
		stopExpireTask();
		owner = null;
		name = null;
		value = null;
		expire_time = 0;
	}

	private static class PlayerVarExpireTask implements Runnable
	{
		private PlayerVar _pv;

		public PlayerVarExpireTask(PlayerVar pv)
		{
			_pv = pv;
		}

		@Override
		public void run()
		{
			final PlayerVar pv = _pv;
			_pv = null;

			if (pv == null)
				return;

			final Player pc = pv.getOwner();
			if (pc == null || pc.isOnline())
			{
				pv.cleanup();
				return;
			}

			PlayerVariables.unsetVar(pc, pv.getName());
		}
	}

	public boolean isNumeric(String str)
	{
		if (str == null)
			return false;

		try
		{
			Integer.parseInt(str);
		}
		catch (NumberFormatException nfe)
		{
			return false;
		}
		return true;
	}
}
