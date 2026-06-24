package net.sf.l2j.gameserver.scriptings.tasks;

import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.instancemanager.SevenSigns;
import net.sf.l2j.gameserver.instancemanager.SevenSignsFestival;
import net.sf.l2j.gameserver.scriptings.Quest;

/**
 * @author Hasha
 */
public final class SevenSignsUpdate extends Quest implements Runnable
{
	public SevenSignsUpdate()
	{
		super(-1, "tasks");
		
		ThreadPool.scheduleAtFixedRate(this, 1800000, 1800000);
	}
	
	@Override
	public final void run()
	{
		try
		{
			SevenSigns.getInstance().saveSevenSignsStatus();
			
			if (!SevenSigns.getInstance().isSealValidationPeriod())
				SevenSignsFestival.getInstance().saveFestivalData(false);
			
			_log.info("SevenSigns: Data updated successfully.");
		}
		catch (Exception e)
		{
			_log.warning("SevenSigns: Failed to save Seven Signs configuration: " + e);
		}
	}
}