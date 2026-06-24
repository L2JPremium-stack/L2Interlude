package net.sf.l2j.gameserver.taskmanager;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList;

/**
 * Periodically updates known list of all existing {@link Creature}.<br>
 * Special scope is used for {@link L2WorldRegion} without {@link Player} inside.
 *
 * Memory-safe version: invalid/off-world creatures are cleaned instead of keeping
 * stale references inside knownlists, and one bad object can no longer break the
 * whole update pass.
 */
public final class KnownListUpdateTaskManager implements Runnable
{
	private static final Logger LOGGER = Logger.getLogger(KnownListUpdateTaskManager.class.getName());
	
	// Update for NPCs is performed each FULL_UPDATE tick interval.
	private static final int FULL_UPDATE = 10;
	
	private boolean _flagForgetAdd = true;
	private int _timer = FULL_UPDATE;
	
	public static final KnownListUpdateTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected KnownListUpdateTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(this, Config.KNOWNLIST_UPDATE_INTERVAL, Config.KNOWNLIST_UPDATE_INTERVAL);
	}
	
	@Override
	public final void run()
	{
		if (--_timer == 0)
			_timer = FULL_UPDATE;
		
		final boolean fullUpdate = _timer < 3;
		_flagForgetAdd = !_flagForgetAdd;
		
		try
		{
			for (L2WorldRegion[] regions : L2World.getInstance().getWorldRegions())
			{
				for (L2WorldRegion region : regions)
				{
					if (region == null)
						continue;
					
					if (!region.isActive() && !fullUpdate)
						continue;
					
					for (L2Object object : region.getVisibleObjects().values())
						processObject(region, object, fullUpdate);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "KnownListUpdateTaskManager: update pass failed.", e);
		}
	}
	
	private void processObject(L2WorldRegion region, L2Object object, boolean fullUpdate)
	{
		if (!(object instanceof Creature))
			return;
		
		final ObjectKnownList knownList = object.getKnownList();
		if (knownList == null)
			return;
		
		if (!object.isVisible())
		{
			knownList.removeAllKnownObjects();
			return;
		}
		
		final boolean isPlayable = object instanceof Playable;
		final boolean isAttackable = object instanceof Attackable;
		
		if (!fullUpdate && !isPlayable && !isAttackable)
			return;
		
		try
		{
			if (_flagForgetAdd)
			{
				knownList.forgetObjects();
				return;
			}
			
			for (L2WorldRegion surroundingRegion : region.getSurroundingRegions())
			{
				if (surroundingRegion == null)
					continue;
				
				if (isAttackable && !surroundingRegion.isActive())
					continue;
				
				for (L2Object knownObject : surroundingRegion.getVisibleObjects().values())
				{
					if (knownObject == null || knownObject == object || !knownObject.isVisible())
						continue;
					
					knownList.addKnownObject(knownObject);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "KnownListUpdateTaskManager: failed objectId=" + object.getObjectId(), e);
			knownList.forgetObjects();
		}
	}
	
	private static class SingletonHolder
	{
		protected static final KnownListUpdateTaskManager _instance = new KnownListUpdateTaskManager();
	}
}
