package net.sf.l2j.gameserver.network.clientpackets;

import java.util.List;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.event.bossevent.KTBEvent;
import net.sf.l2j.event.ctf.CTFEvent;
import net.sf.l2j.event.deathmath.DMEvent;
import net.sf.l2j.event.fortress.FOSEvent;
import net.sf.l2j.event.lastman.LMEvent;
import net.sf.l2j.event.tvt.TvTEvent;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.zone.type.L2TimeFarmZone;
import net.sf.l2j.timezone.TimeFarmZoneManager;

public final class RequestRestartPoint extends L2GameClientPacket
{
	protected int _requestedPointType;
	protected boolean _continuation;
	protected static final Location GIRAN_LOCATION_1 = new Location(82856, 148152, -3464);
	protected static final Location GIRAN_LOCATION_2 = new Location(82856, 149080, -3464);
	protected static final Location GIRAN_LOCATION_3 = new Location(81027, 149141, -3472);
	protected static final Location GIRAN_LOCATION_4 = new Location(81032, 148104, -3464);
	
	@Override
	protected void readImpl()
	{
		_requestedPointType = readD();
	}
	
	class DeathTask implements Runnable
	{
		final Player activeChar;
		
		DeathTask(Player _activeChar)
		{
			activeChar = _activeChar;
		}
		
		@Override
		public void run()
		{
			Location loc = null;
			Castle castle = null;
			
			// force
			if (activeChar.isInJail())
				_requestedPointType = 27;
			else if (activeChar.isFestivalParticipant())
				_requestedPointType = 4;
			
			switch (_requestedPointType)
			{
				case 1: // to clanhall
					if (activeChar.getClan() == null || !activeChar.getClan().hasHideout())
					{
						_log.warning(activeChar.getName() + " called RestartPointPacket - To Clanhall while he doesn't have clan / Clanhall.");
						return;
					}
					
					loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, TeleportWhereType.CLAN_HALL);
					
					if (ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()) != null && ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP) != null)
					{
						activeChar.restoreExp(ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP).getLvl());
					}
					break;
				
				case 2: // to castle
					castle = CastleManager.getInstance().getCastle(activeChar);
					
					if (castle != null && castle.getSiege().isInProgress())
					{
						// Siege in progress
						if (castle.getSiege().checkIsDefender(activeChar.getClan()))
							loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, TeleportWhereType.CASTLE);
						// Just in case you lost castle while being dead.. Port to nearest Town.
						else if (castle.getSiege().checkIsAttacker(activeChar.getClan()))
							loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, TeleportWhereType.TOWN);
						else
						{
							_log.warning(activeChar.getName() + " called RestartPointPacket - To Castle while he doesn't have Castle.");
							return;
						}
					}
					else
					{
						if (activeChar.getClan() == null || !activeChar.getClan().hasCastle())
							return;
						
						loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, TeleportWhereType.CASTLE);
					}
					break;
				
				case 3: // to siege HQ
					L2SiegeClan siegeClan = null;
					castle = CastleManager.getInstance().getCastle(activeChar);
					
					if (castle != null && castle.getSiege().isInProgress())
						siegeClan = castle.getSiege().getAttackerClan(activeChar.getClan());
					
					// Not a normal scenario.
					if (siegeClan == null)
					{
						_log.warning(activeChar.getName() + " called RestartPointPacket - To Siege HQ while he doesn't have Siege HQ.");
						return;
					}
					
					// If a player was waiting with flag option and then the flag dies before the
					// player pushes the button, he is send back to closest/second closest town.
					if (siegeClan.getFlags().isEmpty())
						loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, TeleportWhereType.TOWN);
					else
						loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, TeleportWhereType.SIEGE_FLAG);
					break;
				
				case 4: // Fixed or player is a festival participant
					// if (!activeChar.isGM() && !activeChar.isFestivalParticipant())
					// {
					// _log.warning(activeChar.getName() + " called RestartPointPacket - Fixed while he isn't festival participant!");
					// return;
					// }
					if (activeChar.isGM())
					{
						loc = activeChar.getPosition();
					}
					else
					{
						// Tenta obter a zona TimeFarmZone onde o jogador está
						L2TimeFarmZone currentZone = null;
						for (L2TimeFarmZone zone : TimeFarmZoneManager.getZones())
						{
							if (zone.isInsideZone(activeChar))
							{
								currentZone = zone;
								break;
							}
						}
						
						if (currentZone != null)
						{
							List<Location> spawns = currentZone.getSpawns();
							if (!spawns.isEmpty())
							{
								loc = spawns.get(Rnd.get(spawns.size()));
							}
							else
							{
								loc = currentZone.getZoneCenter(); // fallback
							}
						}
						
						else
						{
							int rnd = 1 + (int) (Math.random() * 4);
							if (rnd == 1)
								loc = GIRAN_LOCATION_1;
							else if (rnd == 2)
								loc = GIRAN_LOCATION_2;
							else if (rnd == 3)
								loc = GIRAN_LOCATION_3;
							else if (rnd == 4)
								loc = GIRAN_LOCATION_4;
							else
								loc = GIRAN_LOCATION_1;
						}
					}
					// loc = new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ()); // spawn them where they died
					break;
				
				case 27: // to jail
					if (!activeChar.isInJail())
						return;
					loc = new Location(-114356, -249645, -2984);
					break;
				
				default:
					loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, TeleportWhereType.TOWN);
					break;
			}
			
			// Teleport and revive
			activeChar.setIsIn7sDungeon(false);
			
			if (activeChar.isDead())
				activeChar.doRevive();
			
			activeChar.teleToLocation(loc, 20);
		}
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (KTBEvent.isPlayerParticipant(activeChar.getObjectId()) && KTBEvent.isStarted())
			return;
		
		if (TvTEvent.isPlayerParticipant(activeChar.getObjectId()) && TvTEvent.isStarted())
			return;
		
		if (CTFEvent.isPlayerParticipant(activeChar.getObjectId()) && CTFEvent.isStarted())
			return;
		
		if (LMEvent.isStarted() && LMEvent.isPlayerParticipant(activeChar.getObjectId()))
			return;
		
		if (DMEvent.isStarted() && DMEvent.isPlayerParticipant(activeChar.getObjectId()))
			return;
		
		if (FOSEvent.isStarted() && FOSEvent.isPlayerParticipant(activeChar.getObjectId()))
			return;
		
		if (activeChar.isFakeDeath())
		{
			activeChar.stopFakeDeath(true);
			return;
		}
		
		if (!activeChar.isDead())
		{
			_log.warning("Living player [" + activeChar.getName() + "] called RequestRestartPoint packet.");
			return;
		}
		
		Castle castle = CastleManager.getInstance().getCastle(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		if (castle != null && castle.getSiege().isInProgress())
		{
			if (activeChar.getClan() != null && castle.getSiege().checkIsAttacker(activeChar.getClan()))
			{
				// Schedule respawn delay for attacker
				ThreadPool.schedule(new DeathTask(activeChar), Config.ATTACKERS_RESPAWN_DELAY);
				
				if (Config.ATTACKERS_RESPAWN_DELAY > 0)
					activeChar.sendMessage("You will be teleported in " + Config.ATTACKERS_RESPAWN_DELAY / 1000 + " seconds.");
				
				return;
			}
		}
		
		// run immediately (no need to schedule)
		new DeathTask(activeChar).run();
	}
}