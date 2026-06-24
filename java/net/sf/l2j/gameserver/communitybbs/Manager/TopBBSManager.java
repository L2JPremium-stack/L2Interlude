package net.sf.l2j.gameserver.communitybbs.Manager;

import java.util.Calendar;
import java.util.StringTokenizer;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.communitybbs.service.DailyRewardService;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.xml.CommunityBoardDailyRewardData;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.DailyRewardHolder;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.taskmanager.GameTimeController;

public class TopBBSManager extends BaseBBSManager
{
	protected TopBBSManager()
	{
	}
	
	public static TopBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@Override
	public void parseCmd(String command, Player activeChar)
	{
		if (command.equals("_bbshome"))
		{
			String content = HtmCache.getInstance().getHtm(CB_PATH + getFolder() + "index.htm");
			content = applyPlayerStats(content, activeChar);
			separateAndSend(content, activeChar);
			return;
		}
		
		/*
		 * DAILY REWARD COMMANDS
		 */
		else if (command.startsWith("_bbshome;dailyReward"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			
			st.nextToken(); // _bbshome
			st.nextToken(); // dailyReward
			
			if (!st.hasMoreTokens())
			{
				loadStaticHtm("index.htm", activeChar);
				return;
			}
			
			final String action = st.nextToken();
			
			switch (action.toLowerCase())
			{
				case "next":
				{
					DailyRewardService.getInstance().moveOffset(activeChar, 1, VISIBLE_REWARDS);
					loadStaticHtm("index.htm", activeChar);
					break;
				}
				
				case "prev":
				{
					DailyRewardService.getInstance().moveOffset(activeChar, -1, VISIBLE_REWARDS);
					loadStaticHtm("index.htm", activeChar);
					break;
				}
				
				case "claim":
				{
					if (!st.hasMoreTokens())
						return;
					
					final int day = Integer.parseInt(st.nextToken());
					
					DailyRewardService.getInstance().claim(activeChar, day);
					loadStaticHtm("index.htm", activeChar);
					break;
				}
			}
			
			return;
		}
		
		/*
		 * STATIC HTML
		 */
		else if (command.startsWith("_bbshome;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			
			loadStaticHtm(st.nextToken(), activeChar);
			return;
		}
		
		super.parseCmd(command, activeChar);
	}
	
	@Override
	protected void loadStaticHtm(String path, Player activeChar)
	{
		String content = HtmCache.getInstance().getHtm(CB_PATH + getFolder() + path);
		
		if (content == null)
		{
			separateAndSend("<html><body>File not found: " + path + "</body></html>", activeChar);
			return;
		}
		
		content = applyPlayerStats(content, activeChar);
		
		separateAndSend(content, activeChar);
	}
	
	public String applyPlayerStats(String content, Player activeChar)
	{
		if (content == null || activeChar == null)
			return content;
		
		content = content.replace("%playerName%", activeChar.getName());
		
		content = content.replace("%CP%", (int) activeChar.getStatus().getCurrentCp() + " / " + StringUtil.formatNumber(activeChar.getStat().getMaxCp()));
		content = content.replace("%HP%", (int) activeChar.getStatus().getCurrentHp() + " / " + StringUtil.formatNumber(activeChar.getStat().getMaxHp()));
		content = content.replace("%MP%", (int) activeChar.getStatus().getCurrentMp() + " / " + StringUtil.formatNumber(activeChar.getStat().getMaxMp()));
		
		content = content.replace("%LV%", String.valueOf(activeChar.getStat().getLevel()));
		
		content = content.replace("%clanName%", activeChar.getClan() != null ? activeChar.getClan().getName() : "No");
		content = content.replace("%clanReputation%", activeChar.getClan() != null ? String.valueOf(activeChar.getClan().getReputationScore()) : "0");
		
		content = content.replace("%classeName%", activeChar.getTemplate().getClassName());
		content = content.replace("%Adena%", StringUtil.formatNumber(activeChar.getInventory().getAdena()));
		
		content = content.replace("%pvpPk%", activeChar.getPvpKills() + " / " + activeChar.getPkKills());
		content = content.replace("%fame%", activeChar.getKarma() > 0 ? String.valueOf(activeChar.getKarma()) : "0");
		
		content = content.replace("%playersConected%", String.valueOf(L2World.getInstance().getPlayers().size()));
		content = content.replace("%dailyReward%", buildDailyRewards(activeChar));
		
		int t = GameTimeController.getInstance().getGameTime();
		int h = t / 60;
		int m = t % 60;
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, h);
		cal.set(Calendar.MINUTE, m);
		
		content = content.replace("%time%", MathUtil.formatDate(cal.getTime(), "h:mm a"));
		
		return content;
	}
	
	private static final int VISIBLE_REWARDS = 7;
	
	private static String buildDailyRewards(Player player)
	{
		final StringBuilder sb = new StringBuilder();
		
		final CommunityBoardDailyRewardData data = CommunityBoardDailyRewardData.getInstance();
		final DailyRewardService service = DailyRewardService.getInstance();
		final int totalRewards = data.getRewards().size();
		
		/*
		 * PLAYER CURRENT DAY
		 */
		final int currentDay = service.getCurrentDay(player);
		/*
		 * PLAYER VISUAL OFFSET
		 */
		int offset = service.getOffset(player, VISIBLE_REWARDS);
		
		/*
		 * AUTO CENTER CURRENT DAY
		 */
		if (offset < 0)
		{
			offset = Math.max(0, currentDay - 4);
			
			final int maxOffset = Math.max(0, totalRewards - VISIBLE_REWARDS);
			
			if (offset > maxOffset)
				offset = maxOffset;
			
			player.getMemos().set("dailyRewardOffset", offset);
		}
		
		/*
		 * START TABLE
		 */
		sb.append("<table><tr>");
		
		/*
		 * LEFT SPACE
		 */
		sb.append("<td width=45 height=16></td>");
		
		/*
		 * PREV BUTTON
		 */
		if (offset > 0)
		{
			sb.append("<td width=18>");
			sb.append("<button value=\"\" action=\"bypass _bbshome;dailyReward;prev\" width=16 height=16 back=\"L2UI_CH3.shortcut_prev_over\" fore=\"L2UI_CH3.shortcut_prev\">");
			sb.append("</td>");
		}
		else
		{
			sb.append("<td width=18></td>");
		}
		
		/*
		 * FIRST SPACING
		 */
		sb.append("<td width=45 height=16></td>");
		
		/*
		 * REWARD SLOTS
		 */
		for (int i = 0; i < VISIBLE_REWARDS; i++)
		{
			final int rewardDay = offset + i + 1;
			
			sb.append("<td>");
			
			if (rewardDay <= totalRewards)
			{
				final DailyRewardHolder reward = data.getReward(rewardDay);
				
				if (reward != null)
				{
					final boolean completed = rewardDay < currentDay;
					final boolean current = rewardDay == currentDay;
					final boolean locked = rewardDay > currentDay;
					final boolean canClaim = current && service.canClaim(player, reward);
					
					final String icon = getItemIcon(reward.getItemId());
					
					String back = "L2UI_CH3.skillLock";
					String fore = "L2UI_CH3.skillLock";
					
					String action = "";
					
					/*
					 * COMPLETED
					 */
					if (completed)
					{
						back = "L2UI_CH3.skillLock";
						fore = icon;
					}
					/*
					 * CLAIMABLE
					 */
					else if (canClaim)
					{
						back = "L2UI_CH3.PremiumItemBtn_Down";
						fore = icon;
						
						action = "bypass _bbshome;dailyReward;claim;" + rewardDay;
					}
					/*
					 * CURRENT
					 */
					else if (current)
					{
						back = "L2UI_CH3.PremiumItemBtn";
						fore = icon;
					}
					/*
					 * LOCKED
					 */
					else if (locked)
					{
						back ="L2UI_CH3.skillLock";
						fore =  icon;
					}
					
					/*
					 * INTERNAL SLOT TABLE
					 */
					sb.append("<table border=0 cellpadding=0 cellspacing=0 width=82>");
					
					/*
					 * ICON
					 */
					sb.append("<tr>");
					sb.append("<td width=80 height=38 align=center valign=middle>");
					
					sb.append("<button value=\"\" action=\"");
					sb.append(action);
					sb.append("\" width=33 height=32 back=\"");
					sb.append(back);
					sb.append("\" fore=\"");
					sb.append(fore);
					sb.append("\">");
					
					sb.append("</td>");
					sb.append("</tr>");
					
					/*
					 * STATUS
					 */
					sb.append("<tr>");
					sb.append("<td width=87 height=14 align=center valign=top>");
					
					/*
					 * CLAIMED
					 */
					if (completed)
					{
						sb.append("<img src=L2UI_JDEV.PurchaseIcon width=20 height=17>");
					}
					/*
					 * CURRENT
					 */
					else if (current)
					{
						if (canClaim)
						{
							sb.append("<font color=\"LEVEL\">READY</font>");
						}
						else
						{
							final String cooldown = service.getRemainingCooldown(player);
							
							/*
							 * COOLDOWN
							 */
							if (!cooldown.equals("Available"))
							{
								sb.append("<font color=\"FF9900\">");
								sb.append(cooldown);
								sb.append("</font>");
							}
							else
							{
								final int kills = player.getMemos().getInteger("dailyRewardKills", 0);
								final int playTime = player.getMemos().getInteger("dailyRewardPlayTime", 0);
								
								/*
								 * KILL MISSION
								 */
								if (kills < reward.getMonsterKills())
								{
									sb.append("<font color=\"B09878\">");
									sb.append(kills);
									sb.append("/");
									sb.append(reward.getMonsterKills());
									sb.append("</font>");
								}
								/*
								 * PLAYTIME MISSION
								 */
								else if (playTime < reward.getPlayTime())
								{
									final int remainingMinutes = reward.getPlayTime() - playTime;

									final int hours = remainingMinutes / 60;
									final int minutes = remainingMinutes % 60;

									sb.append("<font color=\"00CCFF\">");

									if (hours > 0)
									{
										sb.append(hours);
										sb.append("h ");
									}

									if (minutes > 0)
									{
										sb.append(minutes);
										sb.append("m");
									}
									else if (hours <= 0)
									{
										sb.append("1m");
									}

									sb.append(" left");
									sb.append("</font>");
								}
								else
								{
									sb.append("<font color=\"LEVEL\">READY</font>");
								}
							}
						}
					}
					/*
					 * LOCKED
					 */
					else
					{
						sb.append("<font color=\"666666\">LOCKED</font>");
					}
					
					sb.append("</td>");
					sb.append("</tr>");
					
					sb.append("</table>");
				}
			}
			
			sb.append("</td>");
		}
		
		/*
		 * LAST SPACING
		 */
		sb.append("<td width=45 height=16></td>");
		
		/*
		 * NEXT BUTTON
		 */
		final int maxOffset = Math.max(0, totalRewards - VISIBLE_REWARDS);
		
		if (offset < maxOffset)
		{
			sb.append("<td width=18>");
			sb.append("<button value=\"\" action=\"bypass _bbshome;dailyReward;next\" width=16 height=16 back=\"L2UI_CH3.shortcut_next_over\" fore=\"L2UI_CH3.shortcut_next\">");
			sb.append("</td>");
		}
		else
		{
			sb.append("<td width=18></td>");
		}
		
		sb.append("</tr></table>");
		
		return sb.toString();
	}
	
	private static String getItemIcon(int itemId)
	{
		final Item item = ItemTable.getInstance().getTemplate(itemId);
		
		if (item == null)
			return "icon.noimage";
		
		return item.getIcon();
	}
	
	@Override
	protected String getFolder()
	{
		return "interlude/";
	}
	
	private static class SingletonHolder
	{
		protected static final TopBBSManager _instance = new TopBBSManager();
	}
}