package net.sf.l2j.gameserver.taskmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.datatables.xml.CommunityBoardDailyRewardData;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.mods.manager.FakePlayerManager;

public class DailyRewardTaskManager implements Runnable
{
	private final Map<Integer, Long> _players = new ConcurrentHashMap<>();

	protected DailyRewardTaskManager()
	{
		/*
		 * RUN EVERY 60 SECONDS
		 */
		ThreadPool.scheduleAtFixedRate(this, 60000, 60000);
	}

	public final void add(Player player)
	{
		_players.put(player.getObjectId(), System.currentTimeMillis());
	}

	public final void remove(Player player)
	{
		_players.remove(player.getObjectId());
	}

	@Override
	public final void run()
	{
		if (_players.isEmpty())
			return;

		final long currentTime = System.currentTimeMillis();

		final long cooldown = CommunityBoardDailyRewardData.getInstance().getSettings().getCooldownHours() * 60L * 60L * 1000L;

		for (Integer objectId : _players.keySet())
		{
			final Player player = L2World.getInstance().getPlayer(objectId);

			if (player == null)
				continue;

			if (!player.isOnline())
				continue;

			/*
			 * IGNORE FAKE PLAYERS
			 */
			if (FakePlayerManager.getInstance().getPlayer(objectId) != null)
				continue;

			/*
			 * LAST CLAIM
			 */
			final long lastClaim = player.getMemos().getLong("dailyRewardLastClaim", 0);

			/*
			 * STILL IN COOLDOWN
			 */
			if (lastClaim > 0)
			{
				if ((currentTime - lastClaim) < cooldown)
					continue;
			}

			/*
			 * CURRENT PLAYTIME
			 */
			int playTime = player.getMemos().getInteger("dailyRewardPlayTime", 0);

			/*
			 * ADD 1 MINUTE
			 */
			playTime++;

			player.getMemos().set("dailyRewardPlayTime", playTime);

			/*
			 * STORE EVERY 5 MINUTES
			 */
			if ((playTime % 5) == 0)
			{
				if (player.getMemos().hasChanges())
					player.getMemos().storeMe();
			}
		}
	}

	public static final DailyRewardTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final DailyRewardTaskManager _instance = new DailyRewardTaskManager();
	}
}