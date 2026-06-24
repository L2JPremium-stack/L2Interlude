package net.sf.l2j.event.tournament;

import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.util.Broadcast;

public abstract class ArenaTask
{
	
	public static int _bossHeading = 0;
	
	/** The _in progress. */
	public static boolean _started = false;
	
	public static boolean _aborted = false;
	
	public static void SpawnEvent()
	{
		Arena1x1.getInstance().clear();
		Arena3x3.getInstance().clear();
		Arena5x5.getInstance().clear();
		Arena9x9.getInstance().clear();
		
	//	spawnNpc1();
	//	spawnNpc2();
		
		
		
		Broadcast.gameAnnounceToOnlinePlayers("Tournament: Event Started");
		Broadcast.gameAnnounceToOnlinePlayers("Tournament: 1x1| 3x3 | 5x5 | 9x9");
		Broadcast.gameAnnounceToOnlinePlayers("Tournament: Duration: " + ArenaConfig.TOURNAMENT_TIME + " minute(s)!");
		
		
		_aborted = false;
		_started = true;
		
		ThreadPool.schedule(Arena1x1.getInstance(), 5000);
		ThreadPool.schedule(Arena3x3.getInstance(), 5000);
		ThreadPool.schedule(Arena5x5.getInstance(), 5000);
		ThreadPool.schedule(Arena9x9.getInstance(), 5000);
		
		waiter(ArenaConfig.TOURNAMENT_TIME * 60 * 1000); // minutes for event time
		
		if (!_aborted)
			finishEvent();
	}

	public static void finishEvent()
	{
		Broadcast.gameAnnounceToOnlinePlayers("Tournament: Event Finished!");
		
	//	unspawnNpc1();
	//	unspawnNpc2();
		_started = false;
		if (!AdminTournament._arena_manual)
		{
			ArenaEvent.getInstance().StartCalculationOfNextEventTime();
		}
		else
		{
			AdminTournament._arena_manual = false;
		}
		
		
		//ArenaEvent.getInstance().StartCalculationOfNextEventTime();

		for (Player player : L2World.getInstance().getPlayers())
		{
			if (player != null && player.isOnline())
			{
				if (player.isArenaProtection())
				{
					ThreadPool.schedule(new Runnable()
					{
						
						@Override
						public void run()
						{
							if (player.isOnline() && !player.isInArenaEvent() && !player.isArenaAttack())
							{
								if (player.isArena1x1())
									Arena1x1.getInstance().remove(player);
								if (player.isArena3x3())
									Arena3x3.getInstance().remove(player);
								if (player.isArena5x5())
									Arena5x5.getInstance().remove(player);
								if (player.isArena9x9())
									Arena9x9.getInstance().remove(player);
								
								player.setArenaProtection(false);
							}
						}
					}, 25000);
				}
				
				CreatureSay cs = new CreatureSay(player.getObjectId(), Say2.PARTY, "[Tournament]", ("Next Tournament: " + ArenaEvent.getInstance().getNextTime()) + " (GMT-3)."); // 8D
				player.sendPacket(cs);
			}
		}
	}
	
	/**
	 * Checks if is _started.
	 * @return the _started
	 */
	public static boolean is_started()
	{
		return _started;
	}
	
	/**
	 * Waiter.
	 * @param interval the interval
	 */
	/*protected static void waiter(long interval)
	{
		long startWaiterTime = System.currentTimeMillis();
		int seconds = (int) (interval / 1000);
		
		while (startWaiterTime + interval > System.currentTimeMillis() && !_aborted)
		{
			seconds--; // Here because we don't want to see two time announce at the same time
			
			switch (seconds)
			{
				case 3600: // 1 hour left
					
					if (_started)
					{
						Broadcast.gameAnnounceToOnlinePlayers("Tournament: Party Event PvP");
						Broadcast.gameAnnounceToOnlinePlayers("Tournament: Teleport in the GK to (Tournament) Zone");
						Broadcast.gameAnnounceToOnlinePlayers("Tournament: Reward: " + ItemTable.getInstance().getTemplate(ArenaConfig.ARENA_REWARD_ID).getName());
						Broadcast.gameAnnounceToOnlinePlayers("Tournament: " + seconds / 60 / 60 + " hour(s) till event finish!");
					}
					break;
				case 1800: // 30 minutes left
				case 900: // 15 minutes left
				case 600: // 10 minutes left
				case 300: // 5 minutes left
				case 240: // 4 minutes left
				case 180: // 3 minutes left
				case 120: // 2 minutes left
				case 60: // 1 minute left
					// removeOfflinePlayers();
					
					if (_started)
					{
						Broadcast.gameAnnounceToOnlinePlayers("Tournament: " + seconds / 60 + " minute(s) till event finish!");
					}
					break;
				case 30: // 30 seconds left
				case 15: // 15 seconds left
				case 10: // 10 seconds left
				case 3: // 3 seconds left
				case 2: // 2 seconds left
				case 1: // 1 seconds left
					if (_started)
						Broadcast.gameAnnounceToOnlinePlayers("Tournament: " + seconds + " second(s) till event finish!");
					
					break;
			}
			
			long startOneSecondWaiterStartTime = System.currentTimeMillis();
			
			// Only the try catch with Thread.sleep(1000) give bad countdown on high wait times
			while (startOneSecondWaiterStartTime + 1000 > System.currentTimeMillis())
			{
				try
				{
					Thread.sleep(1);
				}
				catch (InterruptedException ie)
				{
				}
			}
		}
	}*/
	
	
	
	
	protected static void waiter(long interval)
	{
	    long startWaiterTime = System.currentTimeMillis();
	    int seconds = (int) (interval / 1000);

	    while (startWaiterTime + interval > System.currentTimeMillis() && !_aborted)
	    {
	        seconds--;  // Decrease seconds to avoid sending the same time announcement twice
	        
	        // Broadcasting time remaining at specific intervals
	        if (_started)
	        {
	            switch (seconds)
	            {
	                case 3600:  // 1 hour left
	                    Broadcast.gameAnnounceToOnlinePlayers("Tournament: Party Event PvP");
	                    Broadcast.gameAnnounceToOnlinePlayers("Tournament: Teleport in the GK to (Tournament) Zone");
	                    Broadcast.gameAnnounceToOnlinePlayers("Tournament: Reward: " + ItemTable.getInstance().getTemplate(ArenaConfig.ARENA_REWARD_ID).getName());
	                    Broadcast.gameAnnounceToOnlinePlayers("Tournament: " + seconds / 60 / 60 + " hour(s) till event finish!");
	                    break;

	                case 1800:  // 30 minutes left
	                case 900:   // 15 minutes left
	                case 600:   // 10 minutes left
	                case 300:   // 5 minutes left
	                case 240:   // 4 minutes left
	                case 180:   // 3 minutes left
	                case 120:   // 2 minutes left
	                case 60:    // 1 minute left
	                    Broadcast.gameAnnounceToOnlinePlayers("Tournament: " + seconds / 60 + " minute(s) till event finish!");
	                    break;

	                case 30:    // 30 seconds left
	                case 15:    // 15 seconds left
	                case 10:    // 10 seconds left
	                case 3:     // 3 seconds left
	                case 2:     // 2 seconds left
	                case 1:     // 1 second left
	                    Broadcast.gameAnnounceToOnlinePlayers("Tournament: " + seconds + " second(s) till event finish!");
	                    break;
	            }
	        }

	        // Sleep for 1 second
	        try
	        {
	            Thread.sleep(1000L);  // Wait for 1 second
	        }
	        catch (InterruptedException ie)
	        {
	            // Handle interrupted exception (optional logging can be added here)
	        }
	    }
	}

}