package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.datatables.BufferTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.ICustomByPassHandler;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.L2Summon;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.skills.L2EffectType;

public class VoicedBuffs implements IVoicedCommandHandler, ICustomByPassHandler
{
  //private static final String PARENT_DIR = "data/html/buffer2/";
	private static final String PARENT_DIR = "data/html/mods/buffer/CommandBuffs/";
	private static final String [] _BYPASSCMD = {"doyoubuff"};
	private static final int PAGE_LIMIT = 6;
	@Override
	public String[] getByPassCommands()
	{
		return _BYPASSCMD;
	}
	@Override
	public void handleCommand(String command, Player player, String parameters)
	{
		if (player == null)
		{
			return;
		}
		if(Config.ENABLE_COMMAND_VIP_BUFFS)
    	{
    		if(!player.isVip())
    		{
    			player.sendMessage("You are not VIP member.");
    			return;
    		}
    	}
		if (player.isInsideZone(ZoneId.PVP_CUSTOM) || player.isInsideZone(ZoneId.CASTLE) || player.isInsideZone(ZoneId.HQ) || player.isInsideZone(ZoneId.SIEGE) || OlympiadManager.getInstance().isRegistered(player) || OlympiadManager.getInstance().isRegisteredInComp(player) || player.isParalyzed() || player.isInsideZone(ZoneId.ARENA_EVENT) || player.isInsideZone(ZoneId.RAID_ZONE) || player.isInsideZone(ZoneId.FLAG) || player.isInsideZone(ZoneId.FLAG_AREA_BOSS) || player.isInJail() || player.isArenaAttack() || player.isArenaProtection() || player.isCastingNow() || player.isOlympiadProtection() || player.isInCombat() || player.isInOlympiadMode() || player.isDead())
		{
			//showGiveBuffsWindow(player);
			player.sendMessage("You can not do that.");
			return;
		}
		StringTokenizer st = new StringTokenizer(parameters, " ");
		String currentCommand = st.nextToken();
		
	
		//Cancelar buffs
		if (parameters.equalsIgnoreCase("cancel"))
		{
			player.stopAllEffects();
		}
		else if (parameters.equalsIgnoreCase("cancelPetAll"))
		{
			final L2Summon summon = player.getPet();
			if (summon != null)
			{
	
				player.getPet().stopAllEffects();
				return;
			}
			player.sendMessage("Necessary Summon or Pet");
			
		}
		else if(parameters.startsWith("Chat"))
		{
			String chatIndex = parameters.substring(4).trim();
			
			//synchronized(_visitedPages)
			//{
			//	_visitedPages.put(player.getObjectId(), chatIndex);
			//}
			
			chatIndex = "-" + chatIndex;
			
			if (chatIndex.equals("-0"))
			{
				chatIndex = "";
			}
			
			//String text = HtmCache.getInstance().getHtm("data/html/mods/buffer/CommandBuffs"+chatIndex+".htm");
			String text = HtmCache.getInstance().getHtm("data/html/mods/buffer/CommandBuffs/index"+chatIndex+".htm");
			
			NpcHtmlMessage htm = new NpcHtmlMessage(player.getLastQuestNpcObject());
			htm.setHtml(text);
			player.sendPacket(htm);
			
		}
		//Abrir Scheme pagina 1
		else if (currentCommand.startsWith("support"))
		{
			showGiveBuffsWindow(player);
		}
		//Criar Scheme
		else if (currentCommand.startsWith("createscheme"))
		{
			try
			{
				final String schemeName = st.nextToken();
				if (schemeName.length() > 14)
				{
					player.sendMessage("Scheme's name must contain up to 14 chars. Spaces are trimmed.");
					return;
				}
				
				final Map<String, ArrayList<Integer>> schemes = BufferTable.getInstance().getPlayerSchemes(player.getObjectId());
				if (schemes != null)
				{
					if (schemes.size() == Config.BUFFER_MAX_SCHEMES)
					{
						player.sendMessage("Maximum schemes amount is already reached.");
						return;
					}
					
					if (schemes.containsKey(schemeName))
					{
						player.sendMessage("The scheme name already exists.");
						return;
					}
				}
				
				BufferTable.getInstance().setScheme(player.getObjectId(), schemeName.trim(), new ArrayList<>());

				showGiveBuffsWindow(player);
			}
			catch (Exception e)
			{
				player.sendMessage("Scheme's name must contain up to 14 chars. Spaces are trimmed.");
			}
		}
		//Deletar Macro
		else if (currentCommand.startsWith("deletescheme"))
		{
			try
			{
				final String schemeName = st.nextToken();
				final Map<String, ArrayList<Integer>> schemes = BufferTable.getInstance().getPlayerSchemes(player.getObjectId());
				
				if (schemes != null && schemes.containsKey(schemeName))
					schemes.remove(schemeName);
			}
			catch (Exception e)
			{
				player.sendMessage("This scheme name is invalid.");
			}
			showGiveBuffsWindow(player);
		}
		//Editar macro
		else if (currentCommand.startsWith("editschemes"))
		{
			showEditSchemeWindow(player, st.nextToken(), st.nextToken(), Integer.parseInt(st.nextToken()));
		}
		//Skills scheme
		else if (currentCommand.startsWith("skill"))
		{
			final String groupType = st.nextToken();
			final String schemeName = st.nextToken();
			
			final int skillId = Integer.parseInt(st.nextToken());
			final int page = Integer.parseInt(st.nextToken());
			
			final List<Integer> skills = BufferTable.getInstance().getScheme(player.getObjectId(), schemeName);
			
			if (currentCommand.startsWith("skillselect") && !schemeName.equalsIgnoreCase("none"))
			{
				if (skills.size() < player.getMaxBuffCount())
					skills.add(skillId);
				else
					player.sendMessage("This scheme has reached the maximum amount of buffs.");
			}
			else if (currentCommand.startsWith("skillunselect"))
				skills.remove(Integer.valueOf(skillId));
			
			showEditSchemeWindow(player, groupType, schemeName, page);
		}
		else if (currentCommand.startsWith("givebuffs"))
		{
			final String schemeName = st.nextToken();
			final int cost = Integer.parseInt(st.nextToken());
			
			Creature target = null;
			if (st.hasMoreTokens())
			{
				final String targetType = st.nextToken();
				if (targetType != null && targetType.equalsIgnoreCase("pet"))
					target = player.getPet();
					showGiveBuffsWindow(player);
			}
			else
				target = player;
			
			if (target == null)
				player.sendMessage("You don't have a pet.");
			else if (cost == 0 || player.reduceAdena("NPC Buffer", cost, target, false))
			{
				for (int skillId : BufferTable.getInstance().getScheme(player.getObjectId(), schemeName))
					SkillTable.getInstance().getInfo(skillId, SkillTable.getInstance().getMaxLevel(skillId)).getEffects(target, target);
					showGiveBuffsWindow(player);
					
			}
		}
	//	super.onBypassFeedback(player, command);
//	}
		//Menu pra remove buffs 1 em 1
		else if (parameters.compareTo("RemoveMenu") == 0)
		{
			showBuffs(player);
		}
		//Remove buff 1 em 1
		else if (parameters.startsWith("RemoveOne"))
		{
			if (st.hasMoreTokens())
			{
				int SkillId = 0;
				
				try
				{
					SkillId = Integer.parseInt(st.nextToken());
				}
				catch(NumberFormatException e)
				{
					return;
				}
				removeBuff(player, SkillId);
			}
		}
		//heal
		else if (currentCommand.startsWith("heal"))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
			
			final L2Summon summon = player.getPet();
			if (summon != null)
				summon.setCurrentHpMp(summon.getMaxHp(), summon.getMaxMp());

		}
		else if (currentCommand.equalsIgnoreCase("fighter"))
		{
			for (Integer skillid : Config.FIGHTER_BUFF_LIST)
			{
				L2Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getMaxLevel(skillid));
				if (skill != null)
					skill.getEffects(player, player);
			}
			
		}
		else if (currentCommand.equalsIgnoreCase("mage"))
		{
			
			for (Integer skillid : Config.MAGE_BUFF_LIST)
			{
				L2Skill skill = SkillTable.getInstance().getInfo(skillid, SkillTable.getInstance().getMaxLevel(skillid));
				if (skill != null)
					skill.getEffects(player, player);
			}
			
		}
	}

	/**
	 * Sends an html packet to player with Give Buffs menu info for player and pet, depending on targetType parameter {player, pet}
	 * @param player : The player to make checks on.
	 */
	private static void showGiveBuffsWindow(Player player)
	{
		final StringBuilder sb = new StringBuilder(200);
		final Map<String, ArrayList<Integer>> schemes = BufferTable.getInstance().getPlayerSchemes(player.getObjectId());
		if (schemes == null || schemes.isEmpty())
			sb.append("<font color=\"LEVEL\">You haven't defined any scheme.</font>");
		else
		{
			for (Map.Entry<String, ArrayList<Integer>> scheme : schemes.entrySet())
			{
				final int cost = getFee(scheme.getValue());
				StringUtil.append(sb, "<font color=\"LEVEL\">", scheme.getKey(), " [", scheme.getValue().size(), " / ", player.getMaxBuffCount(), "]", ((cost > 0) ? " - cost: " + StringUtil.formatNumber(cost) : ""), "</font><br1>");
				StringUtil.append(sb, "<a action=\"bypass  custom_doyoubuff givebuffs ", scheme.getKey(), " ", cost, "\">Use on Me</a>&nbsp;|&nbsp;");
				StringUtil.append(sb, "<a action=\"bypass  custom_doyoubuff givebuffs ", scheme.getKey(), " ", cost, " pet\">Use on Pet</a>&nbsp;|&nbsp;");
				StringUtil.append(sb, "<a action=\"bypass  custom_doyoubuff editschemes Buffs ", scheme.getKey(), " 1\">Edit</a>&nbsp;|&nbsp;");
				StringUtil.append(sb, "<a action=\"bypass  custom_doyoubuff deletescheme ", scheme.getKey(), "\">Delete</a><br>");
			}
		}
		
		String htmFile = "data/html/mods/buffer/CommandBuffs/index-99.htm";

		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setFile(htmFile);
		player.sendPacket(msg);
		
		msg.replace("%schemes%", sb.toString());
		msg.replace("%max_schemes%", Config.BUFFER_MAX_SCHEMES);
	}
	/**
	 * @param list : A list of skill ids.
	 * @return a global fee for all skills contained in list.
	 */
	private static int getFee(ArrayList<Integer> list)
	{
		if (Config.BUFFER_STATIC_BUFF_COST > 0)
			return list.size() * Config.BUFFER_STATIC_BUFF_COST;
		
		int fee = 0;
		for (int sk : list)
			fee += BufferTable.getInstance().getAvailableBuff(sk).getValue();
		
		return fee;
	}
	/**
	 * @param groupType : The group of skills to select.
	 * @param schemeName : The scheme to make check.
	 * @return a string representing all groupTypes available. The group currently on selection isn't linkable.
	 */
	private static String getTypesFrame(String groupType, String schemeName)
	{
		final StringBuilder sb = new StringBuilder(500);
		sb.append("<table>");
		
		int count = 0;
		for (String type : BufferTable.getInstance().getSkillTypes())
		{
			if (count == 0)
				sb.append("<tr>");
			
			if (groupType.equalsIgnoreCase(type))
				StringUtil.append(sb, "<td width=65>", type, "</td>");
			else
				StringUtil.append(sb, "<td width=65><a action=\"bypass  custom_doyoubuff editschemes ", type, " ", schemeName, " 1\">", type, "</a></td>");
			
			count++;
			if (count == 4)
			{
				sb.append("</tr>");
				count = 0;
			}
		}
		
		if (!sb.toString().endsWith("</tr>"))
			sb.append("</tr>");
		
		sb.append("</table>");
		
		return sb.toString();
	}
	/**
	 * This sends an html packet to player with Edit Scheme Menu info. This allows player to edit each created scheme (add/delete skills)
	 * @param player : The player to make checks on.
	 * @param groupType : The group of skills to select.
	 * @param schemeName : The scheme to make check.
	 * @param page The page.
	 */
	private static void showEditSchemeWindow(Player player, String groupType, String schemeName, int page)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		final List<Integer> schemeSkills = BufferTable.getInstance().getScheme(player.getObjectId(), schemeName);
		
		//html.setFile(getHtmlPath(getNpcId(), 100));
		html.setFile("data/html/mods/buffer/CommandBuffs/index-100.htm");
		html.replace("%schemename%", schemeName);
		html.replace("%count%", schemeSkills.size() + " / " + player.getMaxBuffCount());
		html.replace("%typesframe%", getTypesFrame(groupType, schemeName));
		html.replace("%skilllistframe%", getGroupSkillList(player, groupType, schemeName, page));
		//html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
	}
	/**
	 * @param player : The player to make checks on.
	 * @param groupType : The group of skills to select.
	 * @param schemeName : The scheme to make check.
	 * @param page The page.
	 * @return a String representing skills available to selection for a given groupType.
	 */
	private static String getGroupSkillList(Player player, String groupType, String schemeName, int page)
	{
		BufferTable.getInstance();
		// Retrieve the entire skills list based on group type.
		List<Integer> skills = BufferTable.getInstance().getSkillsIdsByType(groupType);
		if (skills.isEmpty())
			return "That group doesn't contain any skills.";
		
		// Calculate page number.
		final int max = MathUtil.countPagesNumber(skills.size(), PAGE_LIMIT);
		if (page > max)
			page = max;
		
		// Cut skills list up to page number.
		skills = skills.subList((page - 1) * PAGE_LIMIT, Math.min(page * PAGE_LIMIT, skills.size()));
		
		final List<Integer> schemeSkills = BufferTable.getInstance().getScheme(player.getObjectId(), schemeName);
		final StringBuilder sb = new StringBuilder(skills.size() * 150);
		
		int row = 0;
		for (int skillId : skills)
		{
			sb.append(((row % 2) == 0 ? "<table width=\"280\" bgcolor=\"000000\"><tr>" : "<table width=\"280\"><tr>"));
			
			if (skillId < 100)
			{
				if (schemeSkills.contains(skillId))
					StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill00", skillId, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferTable.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass  custom_doyoubuff skillunselect ", groupType, " ", schemeName, " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>");
				else
					StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill00", skillId, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferTable.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass  custom_doyoubuff skillselect ", groupType, " ", schemeName, " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>");
			}
			else if (skillId < 1000)
			{
				if (schemeSkills.contains(skillId))
					StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill0", skillId, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferTable.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass  custom_doyoubuff skillunselect ", groupType, " ", schemeName, " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>");
				else
					StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill0", skillId, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferTable.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass  custom_doyoubuff skillselect ", groupType, " ", schemeName, " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>");
			}
			else if (skillId == 4551 || skillId == 4552 || skillId == 4553 || skillId == 4554)
			{
				if (schemeSkills.contains(skillId))
					StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill", 1164, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferTable.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass  custom_doyoubuff skillunselect ", groupType, " ", schemeName, " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>");
				else
					StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill", 1164, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferTable.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass  custom_doyoubuff skillselect ", groupType, " ", schemeName, " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>");
			}
			else if (skillId == 4702 || skillId == 4703)
			{
				if (schemeSkills.contains(skillId))
					StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill", 1332, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferTable.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass  custom_doyoubuff skillunselect ", groupType, " ", schemeName, " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>");
				else
					StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill", 1332, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferTable.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass  custom_doyoubuff skillselect ", groupType, " ", schemeName, " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>");
			}
			else if (skillId == 4699 || skillId == 4700)
			{
				if (schemeSkills.contains(skillId))
					StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill", 1331, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferTable.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass  custom_doyoubuff skillunselect ", groupType, " ", schemeName, " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>");
				else
					StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill", 1331, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferTable.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass  custom_doyoubuff skillselect ", groupType, " ", schemeName, " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>");
			}
			else
			{
				if (schemeSkills.contains(skillId))
					StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill", skillId, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferTable.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass  custom_doyoubuff skillunselect ", groupType, " ", schemeName, " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>");
				else
					StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill", skillId, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferTable.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass  custom_doyoubuff skillselect ", groupType, " ", schemeName, " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>");
			}
			
			sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
			row++;
		}
		
		// Build page footer.
		sb.append("<br><img src=\"L2UI.SquareGray\" width=277 height=1><table width=\"100%\" bgcolor=000000><tr>");
		
		if (page > 1)
		{
			StringUtil.append(sb, "<td align=left width=70><a action=\"bypass  custom_doyoubuff editschemes ", groupType, " ", schemeName, " ", page - 1, "\">Previous</a></td>");
		}
		else
			StringUtil.append(sb, "<td align=left width=70>Previous</td>");
		
		StringUtil.append(sb, "<td align=center width=100>Page ", page, "</td>");
		
		if (page < max)
		{
			StringUtil.append(sb, "<td align=right width=70><a action=\"bypass  custom_doyoubuff editschemes ", groupType, " ", schemeName, " ", page + 1, "\">Next</a></td>");
		}
		else
			StringUtil.append(sb, "<td align=right width=70>Next</td>");
		
		sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
		
		return sb.toString();
	}
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (activeChar == null)
			return  false;
		
		if(Config.ENABLE_COMMAND_VIP_BUFFS)
    	{
    		if(!activeChar.isVip())
    		{
    			activeChar.sendMessage("You are not VIP member.");
    			return false;
    		}
    	}
		
		if (activeChar.isInsideZone(ZoneId.PVP_CUSTOM) || activeChar.isInsideZone(ZoneId.CASTLE) || activeChar.isInsideZone(ZoneId.HQ) || activeChar.isInsideZone(ZoneId.SIEGE) || OlympiadManager.getInstance().isRegistered(activeChar) || OlympiadManager.getInstance().isRegisteredInComp(activeChar) || activeChar.isParalyzed() || activeChar.isInsideZone(ZoneId.ARENA_EVENT) || activeChar.isInsideZone(ZoneId.RAID_ZONE) || activeChar.isInsideZone(ZoneId.FLAG) || activeChar.isInsideZone(ZoneId.FLAG_AREA_BOSS) || activeChar.isInJail() || activeChar.isArenaAttack() || activeChar.isArenaProtection() || activeChar.isCastingNow() || activeChar.isOlympiadProtection() || activeChar.isInCombat() || activeChar.isInOlympiadMode() || activeChar.isDead())
		{
			activeChar.sendMessage("You can not do that.");
			return false;
		}
		if (command.compareTo(Config.BUFFER_COMMAND2) == 0)
		{
			String index = "";
			if ( target != null && target.length() != 0)
			{
				if (!target.equals("0"))
				{
					index= "-"+target;
				}
			}
		
		String text = HtmCache.getInstance().getHtm(PARENT_DIR + "index"+index+".htm");
		NpcHtmlMessage htm = new NpcHtmlMessage(activeChar.getLastQuestNpcObject());
		htm.setHtml(text);
		activeChar.sendPacket(htm);
		}
		return false;
	}
	private void removeBuff(Player player, int SkillId)
	{
		if (player != null && SkillId > 0)
		{
			L2Effect[] effects = player.getAllEffects();
			for (L2Effect e : effects)
			{
				if(e != null && e.getSkill().getId() == SkillId)
				{
					e.exit(true);
					player.sendMessage("Removed buff: " + e.getSkill().getName() + " Level: " + e.getSkill().getLevel());
					player.sendPacket(new ExShowScreenMessage("Removed buff: " + e.getSkill().getName() + " Level: " + e.getSkill().getLevel(), 2000, 2, false));
					player.broadcastUserInfo();
				}
			}
			showBuffs(player);
		}
	}
	public void showBuffs(Player player)
	{
		int count = 0;
		NpcHtmlMessage ms = new NpcHtmlMessage(1);
		ms.setFile(PARENT_DIR + "buffer-player-remove.htm");
		
		ms.replace("%name%", player.getName());
		
		StringBuilder t = new StringBuilder();
		
		L2Effect[] effects = player.getAllEffects();
		
		t.append("<table><tr>");
		
		for (L2Effect e : effects)
		{
			if (e != null && e.getEffectType() == L2EffectType.BUFF)
			{
				count++;
				
				int skillId = e.getSkill().getId();
				String BuffId = String.valueOf(e.getSkill().getId());
				
				if (skillId < 1000)
				{
					BuffId = "0"+BuffId;
				}
				
				if (skillId < 100)
				{
					BuffId = "0"+BuffId;
				}
				if (skillId == 4551 || skillId == 4552 || skillId == 4553 || skillId == 4554)
				{
					BuffId = "1164";
				}
				
				if (skillId == 4702 || skillId == 4703)
				{
					BuffId = "1332";
				}
				
				if (skillId == 4699 || skillId == 4700)
				{
					BuffId = "1331";
				}
				
				t.append("<td><button action=\"bypass  custom_doyoubuff RemoveOne "+skillId+"\" width=32 height=32 back=\"Icon.skill"+BuffId+"\" fore=\"Icon.skill"+BuffId+"\"></td>");
				
				if (count == 8 || count == 16 || count == 24 || count == 32)
				{
					t.append("</tr></table><table><tr>");
				}
			}
		}
		t.append("</tr></table>");
		
		ms.replace("%buffs%", t.toString());
		player.sendPacket(ms);
	}
	
	/**
	 * @param player 
	 * 
	 */
	public static void OpenHtmlBuffsList(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/buffer/CommandBuffs/Buffs.htm");
		player.sendPacket(html);
	}
	public static void OpenHtmlSongsList(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/buffer/CommandBuffs/Songs.htm");
		player.sendPacket(html);
	}
	public static void OpenHtmlDancesList(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/buffer/CommandBuffs/Dances.htm");
		player.sendPacket(html);
	}
	public static void OpenHtmlChantsList(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/buffer/CommandBuffs/Chants.htm");
		player.sendPacket(html);
	}
	public static void OpenHtmlSpecialList(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/buffer/CommandBuffs/Special.htm");
		player.sendPacket(html);
	}
	public static void OpenHtmlProtectList(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/buffer/CommandBuffs/Protect.htm");
		player.sendPacket(html);
	}
	@Override
	public String[] getVoicedCommandList()
	{
		return new String[] {Config.BUFFER_COMMAND2};
	}
}