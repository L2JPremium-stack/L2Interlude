package net.sf.l2j.gameserver.scriptings.tasks;

import net.sf.l2j.gameserver.Shutdown;
import net.sf.l2j.gameserver.scriptings.ScheduledQuest;

/**
 * @author Hasha
 */
public final class ServerShutdown extends ScheduledQuest
{
	private static final int PERIOD = 600; // 10 minutes
	
	public ServerShutdown()
	{
		super(-1, "tasks");
	}
	
	@Override
	public final void onStart()
	{
		new Shutdown(PERIOD, false).start();
	}
	
	@Override
	public final void onEnd()
	{
	}
}