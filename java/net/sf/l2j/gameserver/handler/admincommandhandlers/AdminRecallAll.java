package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.event.bossevent.KTBEvent;
import net.sf.l2j.event.ctf.CTFEvent;
import net.sf.l2j.event.deathmath.DMEvent;
import net.sf.l2j.event.fortress.FOSEvent;
import net.sf.l2j.event.tvt.TvTEvent;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.ZoneId;

public class AdminRecallAll implements IAdminCommandHandler

{
	private static final String[] ADMIN_COMMANDS = { "admin_recallall" };
	
	private static void teleportTo(Player player, int x, int y, int z)
	{
		if (player.isSellBuff() || CTFEvent.isPlayerParticipant(player.getObjectId()) && CTFEvent.isStarted() || TvTEvent.isPlayerParticipant(player.getObjectId()) && TvTEvent.isStarted() || FOSEvent.isPlayerParticipant(player.getObjectId()) && FOSEvent.isStarted() || DMEvent.isPlayerParticipant(player.getObjectId()) && DMEvent.isStarted() || KTBEvent.isPlayerParticipant(player.getObjectId()) && KTBEvent.isStarted() || player.isAlikeDead() || player.isInArenaEvent() || player.isArenaProtection() || player.isOlympiadProtection() || player.isInStoreMode() || player.isRooted() || player.getKarma() > 0 || player.isInOlympiadMode() || player.isOff() || player.isOffShop() || player.isFestivalParticipant() || player.isArenaAttack() || player.isInsideZone(ZoneId.BOSS) || player.isInsideZone(ZoneId.VIP_ZONE) ||  player.isInsideZone(ZoneId.FLAG_AREA_BOSS) || player.isInsideZone(ZoneId.ARENA_EVENT) || player.isInsideZone(ZoneId.PVP_CUSTOM) || player.isInsideZone(ZoneId.RAID_ZONE) || player.isInsideZone(ZoneId.SIEGE) || player.isInsideZone(ZoneId.SPOIL_AREA))
			return;
		
		player.teleToLocation(x, y, z, 0);
	}
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{

		if (command.startsWith("admin_recallall"))	
		{
			for(Player players : L2World.getInstance().getPlayers())
			{
				teleportTo(players, activeChar.getX(), activeChar.getY(), activeChar.getZ());
			}
		}
		
		return false;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}