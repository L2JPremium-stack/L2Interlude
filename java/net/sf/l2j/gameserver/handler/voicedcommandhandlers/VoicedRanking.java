package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.clan.ranking.ClanRankingConfig;
import net.sf.l2j.clan.ranking.TaskClanRankingReward;
import net.sf.l2j.events.eventpvp.PvPEvent;
import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
//import net.sf.l2j.gameserver.model.entity.events.toppvpevent.PvPEvent;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class VoicedRanking implements IVoicedCommandHandler
{
	int pos;
	public static final Logger _log = Logger.getLogger(VoicedRanking.class.getName());
	private static final String[] VOICED_COMMANDS =
	{
		"pvp",
		"pks",
		"clan",
		"ranking",
		// "clanpoint",
		"pvpEvent",
		"pvpevent"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (command.equals("ranking"))
			showRankingHtml(activeChar);
		
		if ((command.equals("pvpEvent") || command.equals("pvpevent")) && Config.PVP_EVENT_ENABLED)
		{
			PvPEvent.getTopHtml(activeChar);
		}
		if (command.equals("pvp"))
		{
			NpcHtmlMessage htm = new NpcHtmlMessage(5);
			StringBuilder tb = new StringBuilder("<html><head><title>Ranking PvP</title></head><body><center><img src=\"l2ui_ch3.herotower_deco\" width=256 height=32></center><br1><table width=290><tr><td><center>Rank</center></td><td><center>Character</center></td><td><center>Pvp's</center></td><td><center>Status</center></td></tr>");
			try (Connection con = ConnectionPool.getConnection())
			{
				PreparedStatement statement = con.prepareStatement("SELECT char_name,pvpkills,online FROM characters WHERE pvpkills>0 AND accesslevel=0 order by pvpkills desc limit 15");
				ResultSet result = statement.executeQuery();
				int pos = 0;
				
				while (result.next())
				{
					String pvps = result.getString("pvpkills");
					String name = result.getString("char_name");
					pos += 1;
					String statu = result.getString("online");
					String status;
					
					if (statu.equals("1"))
						status = "<font color=00FF00>Online</font>";
					else
						status = "<font color=FF0000>Offline</font>";
					
					tb.append("<tr><td><center><font color =\"AAAAAA\">" + pos + "</td><td><center><font color=00FFFF>" + name + "</font></center></td><td><center>" + pvps + "</center></td><td><center>" + status + "</center></td></tr>");
				}
				statement.close();
				result.close();
				con.close();
			}
			catch (Exception e)
			{
				// _log.log(Level.WARNING, "ranking (status): could not load statistics informations" + e.getMessage(), e);
			}
			tb.append("</table>");
			tb.append("<br>");
			tb.append("<br>");
			tb.append("<center><a action=\"bypass voiced_ranking\">Back to Rankings</a>");
			tb.append("</center>");
			tb.append("</body></html>");
			htm.setHtml(tb.toString());
			activeChar.sendPacket(htm);
		}
		
		if (command.equals("pks"))
		{
			NpcHtmlMessage htm = new NpcHtmlMessage(5);
			StringBuilder tb = new StringBuilder("<html><head><title>Ranking PK</title></head><body><center><img src=\"l2ui_ch3.herotower_deco\" width=256 height=32></center><br1><table width=290><tr><td><center>Rank</center></td><td><center>Character</center></td><td><center>Pk's</center></td><td><center>Status</center></td></tr>");
			try (Connection con = ConnectionPool.getConnection())
			{
				PreparedStatement statement = con.prepareStatement("SELECT char_name,pkkills,online FROM characters WHERE pvpkills>0 AND accesslevel=0 order by pkkills desc limit 15");
				ResultSet result = statement.executeQuery();
				int pos = 0;
				
				while (result.next())
				{
					String pks = result.getString("pkkills");
					String name = result.getString("char_name");
					pos += 1;
					String statu = result.getString("online");
					String status;
					
					if (statu.equals("1"))
						status = "<font color=00FF00>Online</font>";
					else
						status = "<font color=FF0000>Offline</font>";
					
					tb.append("<tr><td><center><font color =\"AAAAAA\">" + pos + "</td><td><center><font color=00FFFF>" + name + "</font></center></td><td><center>" + pks + "</center></td><td><center>" + status + "</center></td></tr>");
				}
				statement.close();
				result.close();
				con.close();
			}
			catch (Exception e)
			{
				// _log.log(Level.WARNING, "ranking (status): could not load statistics informations" + e.getMessage(), e);
			}
			
			tb.append("</table>");
			tb.append("<br>");
			tb.append("<br>");
			tb.append("<center><a action=\"bypass voiced_ranking\">Back to Rankings</a>");
			tb.append("</center>");
			tb.append("</body></html>");
			htm.setHtml(tb.toString());
			activeChar.sendPacket(htm);
		}
		
		if (command.equals("clan") && ClanRankingConfig.ENABLE_CLAN_RANKING)
		{
			NpcHtmlMessage htm = new NpcHtmlMessage(0);
			StringBuilder tb = new StringBuilder("<html><head><title>Clan Ranking</title></head><body><table width=80><tr><td><button value=\"\" action=\"bypass voiced_ranking \" width=35 height=23 back=\"L2UI_CH3.calculate2_bs_down\" fore=\"L2UI_CH3.calculate2_bs\"></td><td> Back </td></tr></table><br><center>Next Rewarding: <font color=LEVEL>" + TaskClanRankingReward.getTimeToDate() + "</font><br1><table width=290><tr><td><center>Rank</center></td><td><center>Clan Name</center></td><td><center>Raid Point(s)</center></td><td><center>Castle Point(s)</center></td></tr>");
			
			try (Connection con = ConnectionPool.getConnection())
			{
				// Usando try-with-resources para PreparedStatement e ResultSet
				try (PreparedStatement statement = con.prepareStatement("SELECT clan_id, boss_points, siege_points FROM clan_points WHERE ((boss_points + siege_points)>0) ORDER BY (boss_points + siege_points) desc LIMIT 15"); ResultSet result = statement.executeQuery())
				{
					
					int pos = 0;
					while (result.next())
					{
						String raid = result.getString("boss_points");
						String castle = result.getString("siege_points");
						String owner = result.getString("clan_id");
						pos += 1;
						
						// Usando try-with-resources para PreparedStatement e ResultSet dentro deste bloco
						try (PreparedStatement charname = con.prepareStatement("SELECT clan_name FROM clan_data WHERE clan_id=" + owner); ResultSet result2 = charname.executeQuery())
						{
							
							while (result2.next())
							{
								String clan_name = result2.getString("clan_name");
								
								// Lógica de formatação do nome do clã
								if (clan_name.equals("WWWWWWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWWW") || clan_name.equals("WWWWWWWWWWW") || clan_name.equals("WWWWWWWWWW") || clan_name.equals("WWWWWWWWW") || clan_name.equals("WWWWWWWW") || clan_name.equals("WWWWWWW") || clan_name.equals("WWWWWW"))
									clan_name = clan_name.substring(0, 3) + "..";
								else if (clan_name.length() > 14)
									clan_name = clan_name.substring(0, 14) + "..";
								
								tb.append("<tr><td><center>" + pos + "</center></td><td><center><font color=LEVEL>" + clan_name + "</font></center></td><td><center><font color=00FFFF>" + raid + "</font></center></td><td><center><font color=00FF00>" + castle + "</font></center></td></tr>");
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				_log.warning("Error: could not restore clan_points ranking data info: " + e);
				e.printStackTrace();
			}
			
			tb.append("</table>");
			tb.append("</body></html>");
			
			htm.setHtml(tb.toString());
			activeChar.sendPacket(htm);
		}
		return true;
		
	}
	
	private static void showRankingHtml(Player activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/menu/Ranking.htm");
		activeChar.sendPacket(html);
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}