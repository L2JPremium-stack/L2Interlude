package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.AuctionManager;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.CrownManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.zone.type.L2ClanHallZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles all siege commands
 */
public class AdminSiege implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_siege",
		"admin_add_attacker",
		"admin_add_defender",
		"admin_add_guard",
		"admin_list_siege_clans",
		"admin_clear_siege_list",
		"admin_move_defenders",
		"admin_spawn_doors",
		"admin_endsiege",
		"admin_startsiege",
		"admin_setcastle",
		"admin_removecastle",
		"admin_clanhall",
		"admin_clanhallset",
		"admin_clanhalldel",
		"admin_clanhallopendoors",
		"admin_clanhallclosedoors",
		"admin_clanhallteleportself"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		command = st.nextToken(); // Get actual command
		
		// Get castle
		Castle castle = null;
		ClanHall clanhall = null;
		
		if (command.startsWith("admin_clanhall"))
			clanhall = ClanHallManager.getInstance().getClanHallById(Integer.parseInt(st.nextToken()));
		else if (st.hasMoreTokens())
			castle = CastleManager.getInstance().getCastle(st.nextToken());
		
		if (clanhall == null && (castle == null || castle.getCastleId() < 0))
		{
			showCastleSelectPage(activeChar);
			return true;
		}
		
		L2Object target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player)
			player = (Player) target;
		
		if (castle != null)
		{
			if (command.equalsIgnoreCase("admin_add_attacker"))
			{
				if (player == null)
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				else
					castle.getSiege().registerAttacker(player);
			}
			else if (command.equalsIgnoreCase("admin_add_defender"))
			{
				if (player == null)
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				else
					castle.getSiege().registerDefender(player);
			}
			else if (command.equalsIgnoreCase("admin_add_guard"))
			{
				try
				{
					int npcId = Integer.parseInt(st.nextToken());
					castle.getSiege().getSiegeGuardManager().addSiegeGuard(activeChar, npcId);
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Usage: //add_guard npcId");
				}
			}
			else if (command.equalsIgnoreCase("admin_clear_siege_list"))
			{
				castle.getSiege().clearSiegeClan();
			}
			else if (command.equalsIgnoreCase("admin_endsiege"))
			{
				castle.getSiege().endSiege();
			}
			else if (command.equalsIgnoreCase("admin_list_siege_clans"))
			{
				castle.getSiege().listRegisterClan(activeChar);
				return true;
			}
			else if (command.equalsIgnoreCase("admin_move_defenders"))
			{
				activeChar.sendPacket(SystemMessage.sendString("Not implemented yet."));
			}
			else if (command.equalsIgnoreCase("admin_setcastle"))
			{
				if (player == null || player.getClan() == null)
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				else if (player.getClan().hasCastle())
					activeChar.sendMessage(player.getName() + "'s clan already owns a castle.");
				else
				{
					castle.setOwner(player.getClan());
					CrownManager.getInstance().checkCrowns(player.getClan());
				}
			}
			else if (command.equalsIgnoreCase("admin_removecastle"))
			{
				L2Clan clan = ClanTable.getInstance().getClan(castle.getOwnerId());
				if (clan != null)
				{
					castle.removeOwner(clan);
					CrownManager.getInstance().checkCrowns(clan);
				}
				else
					activeChar.sendMessage("Unable to remove castle for this clan.");
			}
			else if (command.equalsIgnoreCase("admin_spawn_doors"))
			{
				castle.spawnDoors(false);
			}
			else if (command.equalsIgnoreCase("admin_startsiege"))
			{
				castle.getSiege().startSiege();
			}
			showSiegePage(activeChar, castle.getName());
		}
		else if (clanhall != null)
		{
			if (command.equalsIgnoreCase("admin_clanhallset"))
			{
				if (player == null || player.getClan() == null)
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				else if (!ClanHallManager.getInstance().isFree(clanhall.getId()))
					activeChar.sendMessage("This ClanHall isn't free!");
				else if (!player.getClan().hasHideout())
				{
					ClanHallManager.getInstance().setOwner(clanhall.getId(), player.getClan());
					if (AuctionManager.getInstance().getAuction(clanhall.getId()) != null)
						AuctionManager.getInstance().getAuction(clanhall.getId()).deleteAuctionFromDB();
				}
				else
					activeChar.sendMessage("You have already a ClanHall!");
			}
			else if (command.equalsIgnoreCase("admin_clanhalldel"))
			{
				if (!ClanHallManager.getInstance().isFree(clanhall.getId()))
				{
					ClanHallManager.getInstance().setFree(clanhall.getId());
					AuctionManager.getInstance().initNPC(clanhall.getId());
				}
				else
					activeChar.sendMessage("This ClanHall is already Free!");
			}
			else if (command.equalsIgnoreCase("admin_clanhallopendoors"))
			{
				clanhall.openCloseDoors(true);
			}
			else if (command.equalsIgnoreCase("admin_clanhallclosedoors"))
			{
				clanhall.openCloseDoors(false);
			}
			else if (command.equalsIgnoreCase("admin_clanhallteleportself"))
			{
				L2ClanHallZone zone = clanhall.getZone();
				if (zone != null)
					activeChar.teleToLocation(zone.getSpawnLoc(), 0);
			}
			showClanHallPage(activeChar, clanhall);
		}
		return true;
	}
	
	private static void showCastleSelectPage(Player activeChar)
	{
		int i = 0;
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/castles.htm");
		
		final StringBuilder sb = new StringBuilder();
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			if (castle != null)
			{
				StringUtil.append(sb, "<td fixwidth=90><a action=\"bypass -h admin_siege ", castle.getName(), "\">", castle.getName(), "</a></td>");
				i++;
			}
			
			if (i > 2)
			{
				sb.append("</tr><tr>");
				i = 0;
			}
		}
		html.replace("%castles%", sb.toString());
		
		// Cleanup sb.
		sb.setLength(0);
		i = 0;
		
		for (ClanHall clanhall : ClanHallManager.getInstance().getClanHalls().values())
		{
			if (clanhall != null)
			{
				StringUtil.append(sb, "<td fixwidth=134><a action=\"bypass -h admin_clanhall ", clanhall.getId(), "\">", clanhall.getName(), "</a></td>");
				i++;
			}
			
			if (i > 1)
			{
				sb.append("</tr><tr>");
				i = 0;
			}
		}
		html.replace("%clanhalls%", sb.toString());
		
		// Cleanup sb.
		sb.setLength(0);
		i = 0;
		
		for (ClanHall clanhall : ClanHallManager.getInstance().getFreeClanHalls().values())
		{
			if (clanhall != null)
			{
				StringUtil.append(sb, "<td fixwidth=134><a action=\"bypass -h admin_clanhall ", clanhall.getId(), "\">", clanhall.getName(), "</a></td>");
				i++;
			}
			
			if (i > 1)
			{
				sb.append("</tr><tr>");
				i = 0;
			}
		}
		html.replace("%freeclanhalls%", sb.toString());
		activeChar.sendPacket(html);
	}
	
	private static void showSiegePage(Player activeChar, String castleName)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/castle.htm");
		html.replace("%castleName%", castleName);
		activeChar.sendPacket(html);
	}
	
	private static void showClanHallPage(Player activeChar, ClanHall clanhall)
	{
		final L2Clan owner = ClanTable.getInstance().getClan(clanhall.getOwnerId());
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/clanhall.htm");
		html.replace("%clanhallName%", clanhall.getName());
		html.replace("%clanhallId%", clanhall.getId());
		html.replace("%clanhallOwner%", (owner == null) ? "None" : owner.getName());
		activeChar.sendPacket(html);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}