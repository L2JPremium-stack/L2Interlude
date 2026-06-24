package net.sf.l2j.dailyreward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.clientpackets.Say2;

public class DailyRewardManager
{
	private static final Logger _log = Logger.getLogger(DailyRewardManager.class.getName());
	private final SimpleDateFormat format = new SimpleDateFormat("HH:mm");
	
	private static class SingleTonHolder
	{
		protected static final DailyRewardManager _instance = new DailyRewardManager();
	}
	
	public static DailyRewardManager getInstance()
	{
		return SingleTonHolder._instance;
	}
	
	public DailyRewardManager()
	{
		loadData();
		scheduleDailyRewardReset();
		DailyRewardData.getInstance();
	}
	
	public void loadData()
	{
		restoreRewardedPlayersObjId();
		restoreRewardedPlayersHWID();
		
	}
	
	public void showBoard(Player player, String file)
	{
		String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/DailyReward/" + file + ".htm");
		content = content.replace("%rewards%", generateDailyRewardsHtml(player));
		
		BaseBBSManager.separateAndSend(content, player);
		
	}
	
	public String generateDailyRewardsHtml(Player player)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<table bgcolor=000000 border=1>");
		
		int line = 0;
		
		sb.append("<tr>");
		int totalReward = DailyRewardData.getInstance().getAllDailyRewads().size();
		int rewardCount = 0;
		for (DailyReward dr : DailyRewardData.getInstance().getAllDailyRewads())
		{
			if (line < 7)
			{
				sb.append("<td align=center width=80>");
				sb.append("<table>");
				sb.append("<tr>");
				sb.append("<td align=center width=72>");
				sb.append("<font color=LEVEL>Day " + dr.getDay() + " - (" + dr.getAmountTxt() + ")</font>");
				sb.append("<button  action=\"bypass bp_getDailyReward " + dr.getDay() + "\" width=32 height=32 back=\"L2UI_CH3.Minimap.mapbutton_zoomin1\" fore=\"" + dr.getIcon() + "\">");
				sb.append(getReceivedStatus(player, dr));
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("</td>");
				line++;
				rewardCount++;
			}
			if (line >= 7)
			{
				line = 0;
				sb.append("</tr>");
				if (rewardCount < totalReward)
					sb.append("<tr>");
			}
			
		}
		sb.append("</table>");
		
		return sb.toString();
	}
	
	public String getReceivedStatus(Player player, DailyReward dr)
	{
		if (dr.getPlayersReceivdList() == null)
		{
			dr.getPlayersReceivdList().addAll(new ArrayList<>());
		}
		if (dr.getPlayersReceivdList().contains(player.getObjectId()))
		{
			return "<font color=00FF00>Received</font><br>";
		}
		else if (getDailyRewardDays(player) < dr.getDay())
		{
			return "<font color=FF0000>Soon!</font><br>";
		}
		return "<font color=LEVEL>AVAILABLE!</font><br>";
	}
	
	public void saveRewardedPlayersObjId()
	{
		Connection con = null;
		PreparedStatement deleteStmt = null;
		PreparedStatement replaceStmt = null;
		
		try
		{
			con = ConnectionPool.getConnection();
			
			// Deletar tudo da tabela
			deleteStmt = con.prepareStatement("DELETE FROM daily_rewarded_players");
			deleteStmt.executeUpdate();
			
			// Preparar a query REPLACE uma vez só
			replaceStmt = con.prepareStatement("REPLACE INTO daily_rewarded_players (day, obj_id) VALUES (?, ?)");
			
			// Para cada daily reward e seus jogadores
			for (DailyReward dr : DailyRewardData.getInstance().getAllDailyRewads())
			{
				for (int objId : dr.getPlayersReceivdList())
				{
					replaceStmt.setInt(1, dr.getDay());
					replaceStmt.setInt(2, objId);
					replaceStmt.executeUpdate();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (replaceStmt != null)
					replaceStmt.close();
			}
			catch (Exception e)
			{
				if (Config.DEBUG_PATH)
					e.printStackTrace();
			}
			try
			{
				if (deleteStmt != null)
					deleteStmt.close();
			}
			catch (Exception e)
			{
				if (Config.DEBUG_PATH)
					e.printStackTrace();
			}
			try
			{
				if (con != null)
					con.close();
			}
			catch (Exception e)
			{
				if (Config.DEBUG_PATH)
					e.printStackTrace();
			}
		}
	}
	
	public void saveRewardedPlayersHWID()
	{
		Connection con = null;
		PreparedStatement deleteStmt = null;
		PreparedStatement replaceStmt = null;
		
		try
		{
			con = ConnectionPool.getConnection();
			
			// Deleta todos os dados da tabela
			deleteStmt = con.prepareStatement("DELETE FROM daily_rewarded_players_hwid");
			deleteStmt.executeUpdate();
			
			// Prepara a query REPLACE só uma vez
			replaceStmt = con.prepareStatement("REPLACE INTO daily_rewarded_players_hwid (day, hwid) VALUES (?, ?)");
			
			for (DailyReward dr : DailyRewardData.getInstance().getAllDailyRewads())
			{
				for (String hwid : dr.getHwidReceivedList())
				{
					replaceStmt.setInt(1, dr.getDay());
					replaceStmt.setString(2, hwid);
					replaceStmt.executeUpdate();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (replaceStmt != null)
					replaceStmt.close();
			}
			catch (Exception e)
			{
				if (Config.DEBUG_PATH)
					e.printStackTrace();
			}
			try
			{
				if (deleteStmt != null)
					deleteStmt.close();
			}
			catch (Exception e)
			{
				if (Config.DEBUG_PATH)
					e.printStackTrace();
			}
			try
			{
				if (con != null)
					con.close();
			}
			catch (Exception e)
			{
				if (Config.DEBUG_PATH)
					e.printStackTrace();
			}
		}
	}
	
	public void restoreRewardedPlayersObjId()
	{
		// Usando try-with-resources para garantir que os recursos sejam fechados automaticamente
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement st = con.prepareStatement("SELECT * FROM daily_rewarded_players"); ResultSet rs = st.executeQuery()) // O ResultSet é aberto dentro do try-with-resources
		{
			DailyReward reward = null;
			
			// Itera sobre os resultados da consulta
			while (rs.next())
			{
				reward = DailyRewardData.getInstance().getDailyRewardByDay(rs.getInt("day"));
				if (reward.getPlayersReceivdList() == null)
				{
					reward.setPlayersReceivdList(new TreeSet<>());
				}
				reward.getPlayersReceivdList().add(rs.getInt("obj_id"));
			}
			
		}
		catch (Exception e)
		{
			_log.warning("[Daily Reward Manager]: Error could not restore rewarded players: " + e);
			e.printStackTrace();
		}
	}
	
	public void restoreRewardedPlayersHWID()
	{
		// Usando try-with-resources para garantir que todos os recursos sejam fechados automaticamente
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement st = con.prepareStatement("SELECT * FROM daily_rewarded_players_hwid"); ResultSet rs = st.executeQuery()) // O ResultSet é aberto dentro do try-with-resources
		{
			DailyReward reward = null;
			
			// Itera sobre os resultados da consulta
			while (rs.next())
			{
				reward = DailyRewardData.getInstance().getDailyRewardByDay(rs.getInt("day"));
				if (reward.getHwidReceivedList() == null)
				{
					reward.setHwidReceivedList(new TreeSet<>());
				}
				reward.getHwidReceivedList().add(rs.getString("hwid"));
			}
			
		}
		catch (Exception e)
		{
			_log.warning("[Daily Reward Manager]: Error could not restore rewarded players: " + e);
			e.printStackTrace();
		}
	}
	
	public boolean canAddDaysForPlayer(Player player)
	{
		if (player.getVariables().get("CanAddDaysForPlayer") == null)
		{
			PlayerVariables.setVar(player, "CanAddDaysForPlayer", "true", -1);
		}
		return player.getVariables().get("CanAddDaysForPlayer").getValueBoolean();
	}
	
	public void checkResetLastReward(Player player)
	{
		if (getDailyRewardDays(player) >= DailyRewardData.getInstance().getAllDailyRewads().size())
		{
			PlayerVariables.setVar(player, "DailyRewards", 1, -1);
		}
		
	}
	
	public void onPlayerEnter(Player player)
	{
		if (canAddDaysForPlayer(player))
		{
			addDailyRewardDay(player);
			checkResetLastReward(player);
			PlayerVariables.changeValue(player, "CanAddDaysForPlayer", "false");
		}
		if (getRewardsToReceiveCount(player) > 0)
			player.sendChatMessage(0, Say2.HERO_VOICE, "[Daily Reward]", "Welcome, " + player.getName() + " you have " + getRewardsToReceiveCount(player) + " Daily Rewards to receive, check our Community Board (ALT+B).");
	}
	
	public int getRewardsToReceiveCount(Player player)
	{
		int rewarded = 0;
		for (Map.Entry<Integer, DailyReward> entry : DailyRewardData.getInstance().getDailyRewards().entrySet())
		{
			if (entry.getValue().getPlayersReceivdList().contains(player.getObjectId()))
			{
				rewarded++;
			}
		}
		return (getDailyRewardDays(player) - rewarded);
	}
	
	public void addReward(Player player, DailyReward dr)
	{
		
		ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), dr.getItemId());
		item.setEnchantLevel(dr.getEnchantLevel());
		
		if (item.isStackable())
		{
			player.addItem("DailyReward", dr.getItemId(), dr.getAmount(), player, true);
		}
		else
		{
			player.addItem("DailyReward", item, player, true);
		}
		
	}
	
	public void tryToGetDailyReward(Player player, DailyReward dr)
	{
		if (!dr.getPlayersReceivdList().contains(player.getObjectId()))
		{
			// if(dr.getHwidReceivedList().contains(player.getClient().getConnection().getInetAddress().getHostAddress()))
			if (dr.getHwidReceivedList().contains(player.getHWID()))
			{
				player.sendChatMessage(0, Say2.HERO_VOICE, "[Daily Reward]", "You Already received this reward in another character.");
				return;
			}
			if (getDailyRewardDays(player) >= dr.getDay())
			{
				addReward(player, dr);
				player.sendChatMessage(0, Say2.HERO_VOICE, "[Daily Reward]", "Congratulations, you received Day " + dr.getDay() + " reward!");
				dr.getPlayersReceivdList().add(player.getObjectId());
				// dr.getHwidReceivedList().add(player.getClient().getConnection().getInetAddress().getHostAddress());
				dr.getHwidReceivedList().add(player.getHWID());
			}
			else
			{
				player.sendChatMessage(0, Say2.HERO_VOICE, "[Daily Reward]", "You can't take this reward yet! Keep logging in for more days to get it.");
				return;
			}
		}
		else
		{
			player.sendChatMessage(0, Say2.HERO_VOICE, "[Daily Reward]", "You already received this reward! ");
		}
		
	}
	
	public void addDailyRewardDay(Player player)
	{
		if (player.getVariables().get("DailyRewards") == null)
		{
			PlayerVariables.setVar(player, "DailyRewards", 0, -1);
		}
		int days = getDailyRewardDays(player) + 1;
		
		PlayerVariables.changeValue(player, "DailyRewards", String.valueOf(days));
		
	}
	
	public int getDailyRewardDays(Player player)
	{
		if (player.getVariables().get("DailyRewards") == null)
		{
			PlayerVariables.setVar(player, "DailyRewards", 1, -1);
			return 1;
		}
		return Integer.parseInt(player.getVariables().get("DailyRewards").getValue());
	}
	
	public String getNextResetTime()
	{
		if (dailyRewardResetTime().getTime() != null)
		{
			return format.format(dailyRewardResetTime().getTime());
		}
		return "Error";
	}
	
	public Calendar dailyRewardResetTime()
	{
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar testStartTime = null;
			long flush2 = 0L;
			long timeL = 0L;
			int count = 0;
			Calendar resetTime = null;
			for (String timeOfDay : Config.DAILY_REWARD_RESET_TIME)
			{
				testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);
				String[] splitTimeOfDay = timeOfDay.split(":");
				testStartTime.set(11, Integer.parseInt(splitTimeOfDay[0]));
				testStartTime.set(12, Integer.parseInt(splitTimeOfDay[1]));
				testStartTime.set(13, 0);
				if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
				{
					testStartTime.add(5, 1);
				}
				timeL = testStartTime.getTimeInMillis() - currentTime.getTimeInMillis();
				if (count == 0)
				{
					flush2 = timeL;
					resetTime = testStartTime;
				}
				if (timeL < flush2)
				{
					flush2 = timeL;
					resetTime = testStartTime;
				}
				count++;
			}
			return resetTime;
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	// public void scheduleDailyRewardReset()
	// {
	// try
	// {
	// Calendar currentTime = Calendar.getInstance();
	// Calendar testStartTime = null;
	// long flush2 = 0L;
	// long timeL = 0L;
	// int count = 0;
	// Calendar resetTime = null;
	// for (String timeOfDay : Config.DAILY_REWARD_RESET_TIME)
	// {
	// testStartTime = Calendar.getInstance();
	// testStartTime.setLenient(true);
	// String[] splitTimeOfDay = timeOfDay.split(":");
	// testStartTime.set(11, Integer.parseInt(splitTimeOfDay[0]));
	// testStartTime.set(12, Integer.parseInt(splitTimeOfDay[1]));
	// testStartTime.set(13, 0);
	// if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
	// {
	// testStartTime.add(5, 1);
	// }
	// timeL = testStartTime.getTimeInMillis() - currentTime.getTimeInMillis();
	// if (count == 0)
	// {
	// flush2 = timeL;
	// resetTime = testStartTime;
	// }
	// if (timeL < flush2)
	// {
	// flush2 = timeL;
	// resetTime = testStartTime;
	// }
	// count++;
	// }
	// ThreadPoolManager.getInstance().scheduleGeneral(new DailyRewardReset(), flush2);
	//
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	//
	// }
	// }
	public void scheduleDailyRewardReset()
	{
		try
		{
			Calendar currentTime = Calendar.getInstance();
			long flush2 = 0L;
			long timeL = 0L;
			int count = 0;
			
			for (String timeOfDay : Config.DAILY_REWARD_RESET_TIME)
			{
				Calendar testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);
				String[] splitTimeOfDay = timeOfDay.split(":");
				testStartTime.set(11, Integer.parseInt(splitTimeOfDay[0]));
				testStartTime.set(12, Integer.parseInt(splitTimeOfDay[1]));
				testStartTime.set(13, 0);
				
				// If the target time is in the past today, schedule for tomorrow
				if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
				{
					testStartTime.add(Calendar.DAY_OF_MONTH, 1);
				}
				
				timeL = testStartTime.getTimeInMillis() - currentTime.getTimeInMillis();
				
				// If this is the first or closest time, we store it
				if (count == 0 || timeL < flush2)
				{
					flush2 = timeL;
				}
				count++;
			}
			
			// Schedule the task after the calculated time delay
			ThreadPool.schedule(new DailyRewardReset(), flush2);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	class DailyRewardReset implements Runnable
	{
		
		@Override
		public void run()
		{
			deleteAllVarsOfName("CanAddDaysForPlayer");
		}
		
	}
	
	public void deleteAllVarsOfName(String name)
	{
		Connection con = null;
		PreparedStatement offline = null;
		
		try
		{
			con = ConnectionPool.getConnection();
			offline = con.prepareStatement("DELETE FROM character_memo_alt WHERE name=?");
			offline.setString(1, name);
			offline.executeUpdate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (offline != null)
					offline.close();
			}
			catch (Exception e)
			{
				if (Config.DEBUG_PATH)
					e.printStackTrace();
			}
			
			try
			{
				if (con != null)
					con.close();
			}
			catch (Exception e)
			{
				if (Config.DEBUG_PATH)
					e.printStackTrace();
			}
		}
	}
}
