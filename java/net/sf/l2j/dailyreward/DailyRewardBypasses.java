package net.sf.l2j.dailyreward;

import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.Player;

public class DailyRewardBypasses implements IBypassHandler
{

	@Override
	public boolean handleBypass(String bypass, Player activeChar)
	{
		StringTokenizer st = new StringTokenizer(bypass, " ");
		st.nextToken();
		if (bypass.startsWith("bp_getDailyReward") && Config.ENABLE_REWARD_DAILY)
		{
			int day = Integer.parseInt(st.nextToken());
			DailyReward dr = DailyRewardData.getInstance().getDailyRewardByDay(day);
			DailyRewardManager.getInstance().tryToGetDailyReward(activeChar, dr);
			DailyRewardManager.getInstance().showBoard(activeChar, "index");
		}
		if (bypass.startsWith("bp_showDailyRewardsBoard") && Config.ENABLE_REWARD_DAILY)
		{

			DailyRewardManager.getInstance().showBoard(activeChar, "index");
		}
		return false;
	}

	@Override
	public String[] getBypassHandlersList()
	{

		return new String[]
		{ "bp_getDailyReward", "bp_showDailyRewardsBoard" };
	}

}
