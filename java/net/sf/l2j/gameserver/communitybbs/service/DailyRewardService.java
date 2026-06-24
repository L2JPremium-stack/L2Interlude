package net.sf.l2j.gameserver.communitybbs.service;

import java.util.concurrent.TimeUnit;

import net.sf.l2j.gameserver.datatables.xml.CommunityBoardDailyRewardData;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.DailyRewardHolder;
import net.sf.l2j.gameserver.model.holder.DailyRewardSettings;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;

public final class DailyRewardService
{
	private final CommunityBoardDailyRewardData _data = CommunityBoardDailyRewardData.getInstance();
	
	public static DailyRewardService getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	public int getCurrentDay(Player player)
	{
		checkRewardReset(player);
		return player.getMemos().getInteger("dailyRewardDay", 1);
	}
	
	public int getOffset(Player player, int visibleRewards)
	{
		final int totalRewards = _data.getRewards().size();
		final int currentDay = getCurrentDay(player);
		
		int offset = player.getMemos().getInteger("dailyRewardOffset", -1);
		
		if (offset < 0)
		{
			offset = Math.max(0, currentDay - 4);
			
			final int maxOffset = Math.max(0, totalRewards - visibleRewards);
			if (offset > maxOffset)
				offset = maxOffset;
			
			player.getMemos().set("dailyRewardOffset", offset);
		}
		
		return offset;
	}
	
	public void moveOffset(Player player, int value, int visibleRewards)
	{
		final int totalRewards = _data.getRewards().size();
		final int maxOffset = Math.max(0, totalRewards - visibleRewards);
		
		int offset = player.getMemos().getInteger("dailyRewardOffset", 0);
		offset += value;
		
		if (offset < 0)
			offset = 0;
		
		if (offset > maxOffset)
			offset = maxOffset;
		
		player.getMemos().set("dailyRewardOffset", offset);
		store(player);
	}
	
	public boolean canClaim(Player player, DailyRewardHolder reward)
	{
		if (reward == null)
			return false;
		
		if (isStreakExpired(player))
		{
			checkRewardReset(player);
			return false;
		}
		
		final int kills = player.getMemos().getInteger("dailyRewardKills", 0);
		final int playTime = player.getMemos().getInteger("dailyRewardPlayTime", 0);
		
		if (kills < reward.getMonsterKills())
			return false;
		
		if (playTime < reward.getPlayTime())
			return false;
		
		return !isOnCooldown(player);
	}
	
	public boolean claim(Player player, int day)
	{
		checkRewardReset(player);
		
		final DailyRewardHolder reward = _data.getReward(day);
		if (reward == null)
			return false;
		
		final int currentDay = player.getMemos().getInteger("dailyRewardDay", 1);
		
		if (day != currentDay)
			return false;
		
		if (isOnCooldown(player))
		{
			player.sendMessage("Daily Reward is still on cooldown.");
			return false;
		}
		
		if (!canClaim(player, reward))
		{
			player.sendMessage("Mission requirements not completed.");
			return false;
		}
		
		player.addItem("DailyReward", reward.getItemId(), (int) reward.getCount(), player, true);
		
		if (reward.getEnchant() > 0 && player.getInventory().getItemByItemId(reward.getItemId()) != null)
			player.getInventory().getItemByItemId(reward.getItemId()).setEnchantLevel(reward.getEnchant());
		
		player.sendPacket(new PlaySound(2, reward.getSound(), 0, 0, player.getX(), player.getY(), player.getZ()));
		
		player.getMemos().set("dailyRewardLastClaim", System.currentTimeMillis());
		
		int nextDay = currentDay + 1;
		if (nextDay > _data.getSettings().getMaxStreak())
			nextDay = 1;
		
		player.getMemos().set("dailyRewardDay", nextDay);
		player.getMemos().set("dailyRewardKills", 0);
		player.getMemos().set("dailyRewardPlayTime", 0);
		
		store(player);
		return true;
	}
	
	public boolean isOnCooldown(Player player)
	{
		final long lastClaim = player.getMemos().getLong("dailyRewardLastClaim", 0);
		if (lastClaim <= 0)
			return false;
		
		final long cooldown = TimeUnit.HOURS.toMillis(_data.getSettings().getCooldownHours());
		return System.currentTimeMillis() - lastClaim < cooldown;
	}
	
	public String getRemainingCooldown(Player player)
	{
		final long lastClaim = player.getMemos().getLong("dailyRewardLastClaim", 0);
		if (lastClaim <= 0)
			return "Available";
		
		final long cooldown = TimeUnit.HOURS.toMillis(_data.getSettings().getCooldownHours());
		final long remaining = (lastClaim + cooldown) - System.currentTimeMillis();
		
		if (remaining <= 0)
			return "Available";
		
		final long hours = TimeUnit.MILLISECONDS.toHours(remaining);
		final long minutes = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60;
		
		return hours + "h " + minutes + "m";
	}
	
	public boolean isStreakExpired(Player player)
	{
		final DailyRewardSettings settings = _data.getSettings();
		
		if (!settings.isResetOnMissedDay())
			return false;
		
		final long lastClaim = player.getMemos().getLong("dailyRewardLastClaim", 0);
		if (lastClaim <= 0)
			return false;
		
		final long grace = TimeUnit.HOURS.toMillis(settings.getGracePeriodHours());
		return System.currentTimeMillis() - lastClaim > grace;
	}
	
	public void checkRewardReset(Player player)
	{
		if (!isStreakExpired(player))
			return;
		
		player.getMemos().set("dailyRewardDay", 1);
		player.getMemos().set("dailyRewardKills", 0);
		player.getMemos().set("dailyRewardPlayTime", 0);
		player.getMemos().set("dailyRewardOffset", -1);
		
		player.sendMessage("Daily reward streak has expired.");
		store(player);
	}
	
	private static void store(Player player)
	{
		if (player.getMemos().hasChanges())
			player.getMemos().storeMe();
	}
	
	private static class SingletonHolder
	{
		private static final DailyRewardService INSTANCE = new DailyRewardService();
	}
}