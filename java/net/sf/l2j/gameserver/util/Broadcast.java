package net.sf.l2j.gameserver.util;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.event.bossevent.KTBEvent;
import net.sf.l2j.event.ctf.CTFEvent;
import net.sf.l2j.event.deathmath.DMEvent;
import net.sf.l2j.event.fortress.FOSEvent;
import net.sf.l2j.event.lastman.LMEvent;
import net.sf.l2j.event.tvt.TvTEvent;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import net.sf.l2j.gameserver.model.SpawnLocation;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.ConfirmDlg;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;

public final class Broadcast
{
	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers of the L2Character that have the Character targetted.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
	 * <BR>
	 * @param character The character to make checks on.
	 * @param mov The packet to send.
	 */
	public static void toPlayersTargettingMyself(Creature character, L2GameServerPacket mov)
	{
		for (Player player : character.getKnownList().getKnownType(Player.class))
		{
			if (player.getTarget() != character)
				continue;
			
			player.sendPacket(mov);
		}
	}
	
	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
	 * <BR>
	 * @param character The character to make checks on.
	 * @param mov The packet to send.
	 */
	public static void toKnownPlayers(Creature character, L2GameServerPacket mov)
	{
		for (Player player : character.getKnownList().getKnownType(Player.class))
			player.sendPacket(mov);
	}
	
	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers (in the specified radius) of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the L2Character, server just needs to go through _knownPlayers to send Server->Client Packet and check the distance between the targets.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
	 * <BR>
	 * @param character The character to make checks on.
	 * @param mov The packet to send.
	 * @param radius The given radius.
	 */
	public static void toKnownPlayersInRadius(Creature character, L2GameServerPacket mov, int radius)
	{
		if (radius < 0)
			radius = 1500;
		
		for (Player player : character.getKnownList().getKnownType(Player.class))
		{
			if (character.isInsideRadius(player, radius, false, false))
				player.sendPacket(mov);
		}
	}
	
	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers of the L2Character and to the specified character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR>
	 * <BR>
	 * @param character The character to make checks on.
	 * @param mov The packet to send.
	 */
	public static void toSelfAndKnownPlayers(Creature character, L2GameServerPacket mov)
	{
		if (character instanceof Player)
			character.sendPacket(mov);
		
		toKnownPlayers(character, mov);
	}
	
	public static void toSelfAndKnownPlayersInRadius(Creature character, L2GameServerPacket mov, int radius)
	{
		if (radius < 0)
			radius = 600;
		
		if (character instanceof Player)
			character.sendPacket(mov);
		
		for (Player player : character.getKnownList().getKnownType(Player.class))
		{
			if (character.isInsideRadius(player, radius, false, false))
				player.sendPacket(mov);
		}
	}
	
	public static void toSelfAndKnownPlayersInRadiusSq(Creature character, L2GameServerPacket mov, int radiusSq)
	{
		if (radiusSq < 0)
			radiusSq = 360000;
		
		if (character instanceof Player)
			character.sendPacket(mov);
		
		for (Player player : character.getKnownList().getKnownType(Player.class))
		{
			if (character.getDistanceSq(player) <= radiusSq)
				player.sendPacket(mov);
		}
	}
	
	/**
	 * Send a packet to all L2PcInstance present in the world.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through _allPlayers to send Server->Client Packet<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
	 * <BR>
	 * @param mov The packet to send.
	 */
	public static void toAllOnlinePlayers(L2GameServerPacket mov)
	{
		for (Player player : L2World.getInstance().getPlayers())
		{
			if (player.isOnline())
				player.sendPacket(mov);
		}
	}
	
	/**
	 * Send a packet to all players in a specific region.
	 * @param region : The region to send packets.
	 * @param packets : The packets to send.
	 */
	public static void toAllPlayersInRegion(L2WorldRegion region, L2GameServerPacket... packets)
	{
		for (Playable playable : region.getVisiblePlayable().values())
		{
			if (playable instanceof Player)
			{
				for (L2GameServerPacket packet : packets)
					playable.sendPacket(packet);
			}
		}
	}
	
	/**
	 * Send a packet to all players in a specific zone type.
	 * @param <T> L2ZoneType.
	 * @param zoneType : The zone type to send packets.
	 * @param packets : The packets to send.
	 */
	public static <T extends L2ZoneType> void toAllPlayersInZoneType(Class<T> zoneType, L2GameServerPacket... packets)
	{
		for (L2ZoneType temp : ZoneManager.getInstance().getAllZones(zoneType))
		{
			for (Player player : temp.getKnownTypeInside(Player.class))
			{
				for (L2GameServerPacket packet : packets)
					player.sendPacket(packet);
			}
		}
	}
	
	public static void announceToOnlinePlayers(String text)
	{
		toAllOnlinePlayers(new CreatureSay(0, Say2.ANNOUNCEMENT, "", text));
	}
	// Colored Announcements 8D
	public static void gameAnnounceEnchantFailed(String text)
	{
		CreatureSay cs = new CreatureSay(0, Say2.TELL, "", text);
		//CreatureSay cs = new CreatureSay(0, 18, "", ""+text);
		for(Player player : L2World.getInstance().getPlayers())
		{
			if(player != null)
				if(player.isOnline())
					player.sendPacket(cs);
		}
	}
	public static void gameAnnounceEnchantSucessful(String text)
	{
		CreatureSay cs = new CreatureSay(0, Config.CHAT_COLOR_ENCHANT, "", text);
		//CreatureSay cs = new CreatureSay(0, 18, "", ""+text);
		for(Player player : L2World.getInstance().getPlayers())
		{
			if(player != null)
				if(player.isOnline())
					player.sendPacket(cs);
		}
	}
	public static void announceToOnlinePlayers(String text, boolean critical)
	{
		toAllOnlinePlayers(new CreatureSay(0, (critical) ? Say2.CRITICAL_ANNOUNCE : Say2.ANNOUNCEMENT, "", text));
	}

	public static void gameAnnounceToOnlinePlayers(String text)
	{
		toAllOnlinePlayers(new CreatureSay(0, Say2.CRITICAL_ANNOUNCE, "", text));
	}
	public static void GameAnnuncieDropItems(String text)
	{
		toAllOnlinePlayers(new CreatureSay(0, Config.DROP_ANNUNCIE_ID, "", text));
	}
	public static void AnnuncieGKSpawnBossEvent(String text)
	{
		toAllOnlinePlayers(new CreatureSay(0, Say2.PARTY, "Event", text));
	}

	public static void PMAnnounce(String text)
	{
		toAllOnlinePlayers(new CreatureSay(0, Say2.CLAN, "", text));
	}

	// Colored Announcements 8D
	public static void gameAnnounceToAll(String text)
	{
		CreatureSay cs = new CreatureSay(0, Say2.PARTY, "", text);
		//CreatureSay cs = new CreatureSay(0, 18, "", ""+text);
		for(Player player : L2World.getInstance().getPlayers())
		{
			if(player != null)
				if(player.isOnline())
					player.sendPacket(cs);
		}
	}

	public static boolean pvp_register = false;
	
	public static void PvPAnnounce(String text)
	{
		pvp_register = true;
		
		CreatureSay cs = new CreatureSay(0, Say2.PARTY, "", "" + text);
		
		for (Player player : L2World.getInstance().getPlayers())
		{
			
			if (player != null && player.isOnline())
			{
				player.sendPacket(cs);
				
				ThreadPool.schedule(new Runnable()
				{
					@Override
					public void run()
					{
						pvp_register = false;
					}
				}, 46000);
				
			//	final boolean bishop = (player.getClassId() == ClassId.BISHOP || player.getClassId() == ClassId.CARDINAL || player.getClassId() == ClassId.SHILLIEN_ELDER || player.getClassId() == ClassId.SHILLIEN_SAINT || player.getClassId() == ClassId.EVAS_SAINT || player.getClassId() == ClassId.ELVEN_ELDER);
				
				if (Config.SCREN_MSG_PVP)
				{
					if (player.inObserverMode() || player.isSellBuff() || player.isAio() || player.isInsideZone(ZoneId.PVP_CUSTOM) || CTFEvent.isPlayerParticipant(player.getObjectId()) && CTFEvent.isStarted()  || TvTEvent.isPlayerParticipant(player.getObjectId()) && TvTEvent.isStarted()  || FOSEvent.isPlayerParticipant(player.getObjectId()) && FOSEvent.isStarted() || DMEvent.isPlayerParticipant(player.getObjectId()) && DMEvent.isStarted() || KTBEvent.isPlayerParticipant(player.getObjectId()) && KTBEvent.isStarted() || LMEvent.isPlayerParticipant(player.getObjectId()) && LMEvent.isStarted() || player.isAlikeDead() || player.isInArenaEvent() || player.isArenaProtection() || player.isOlympiadProtection() || player.isInStoreMode() || player.isRooted() || player.getKarma() > 0 || player.isInOlympiadMode() || player.isOff() || player.isOffShop() || player.isFestivalParticipant() || player.isArenaAttack() || player.isInsideZone(ZoneId.BOSS) || player.isInsideZone(ZoneId.FLAG_AREA_BOSS) || player.isInsideZone(ZoneId.ARENA_EVENT) || player.isInsideZone(ZoneId.PVP_CUSTOM) || player.isInsideZone(ZoneId.RAID_ZONE) || player.isInsideZone(ZoneId.SIEGE) || player.isInsideZone(ZoneId.SPOIL_AREA))
						continue;
					SpawnLocation _position = new SpawnLocation(Config.pvp_locx + Rnd.get(-100, 100), Config.pvp_locy + Rnd.get(-100, 100), Config.pvp_locz, 0);

					ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.S1.getId());
					confirm.addString("Let's go to PvpZone?");
				//	confirm.addZoneName(_position);
					confirm.addZoneName(_position.getX(), _position.getY(), _position.getZ());
					confirm.addTime(45000);
					confirm.addRequesterId(player.getObjectId());
					player.sendPacket(confirm);
				}
			}
		}
		
		cs = null;
	}
	public static void gameAnnounceAuctionShop(String text)
	{
		toAllOnlinePlayers(new CreatureSay(0, Config.COR_LOJA_AUCTION_ANUNCIE, "", text));
	}
	public static void DropBossCustom(String text)
	{
		toAllOnlinePlayers(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "", text));
	}
	public static void announceEventHideToPlayers(String message)
	{
		toAllOnlinePlayers(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "", message));

	}
	public static void ServerAnnounce(String text)
	{
		toAllOnlinePlayers(new CreatureSay(0, 16, "SVR", text));
	}
}