package net.sf.l2j.gameserver.handler.itemhandlers.custom;

import net.sf.l2j.Config;
import net.sf.l2j.event.bossevent.KTBConfig;
import net.sf.l2j.event.bossevent.KTBEvent;
import net.sf.l2j.event.bossevent.KTBManager;
import net.sf.l2j.event.championInvade.ChampionInvade;
import net.sf.l2j.event.championInvade.InitialChampionInvade;
import net.sf.l2j.event.ctf.CTFConfig;
import net.sf.l2j.event.ctf.CTFEvent;
import net.sf.l2j.event.deathmath.DMConfig;
import net.sf.l2j.event.deathmath.DMEvent;
import net.sf.l2j.event.deathmath.DMManager;
import net.sf.l2j.event.fortress.FOSConfig;
import net.sf.l2j.event.fortress.FOSEvent;
import net.sf.l2j.event.fortress.FOSManager;
import net.sf.l2j.event.lastman.CheckNextEvent;
import net.sf.l2j.event.lastman.LMEvent;
import net.sf.l2j.event.partyfarm.InitialPartyFarm;
import net.sf.l2j.event.partyfarm.PartyFarm;
import net.sf.l2j.event.rewardsoloevent.InitialSoloEvent;
import net.sf.l2j.event.rewardsoloevent.RewardSoloEvent;
import net.sf.l2j.event.soloboss.InitialSoloBossEvent;
import net.sf.l2j.event.soloboss.SoloBoss;
import net.sf.l2j.event.spoil.InitialSpoilEvent;
import net.sf.l2j.event.spoil.SpoilEvent;
import net.sf.l2j.event.tournament.ArenaConfig;
import net.sf.l2j.event.tournament.ArenaEvent;
import net.sf.l2j.event.tournament.ArenaTask;
import net.sf.l2j.event.tvt.TvTConfig;
import net.sf.l2j.event.tvt.TvTEvent;
import net.sf.l2j.events.eventpvp.PvPEvent;
import net.sf.l2j.events.eventpvp.PvPEventNext;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;
import net.sf.l2j.mission.MissionReset;

/**
 * @author Christian
 *
 */
public class TeleportBook implements IItemHandler
{

	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		final Player activeChar = (Player) playable;
		
		if (activeChar.isInOlympiadMode() || activeChar.getPvpFlag() > 0 || activeChar.isMoving() || activeChar.isDead() || AttackStanceTaskManager.getInstance().isInAttackStance(activeChar) || activeChar.isCursedWeaponEquipped() || activeChar.isInArenaEvent() || OlympiadManager.getInstance().isRegistered(activeChar) || activeChar.getKarma() > 0 || activeChar.inObserverMode() || CTFEvent.isPlayerParticipant(activeChar.getObjectId()) && CTFEvent.isStarted() || TvTEvent.isPlayerParticipant(activeChar.getObjectId()) && TvTEvent.isStarted() || LMEvent.isPlayerParticipant(activeChar.getObjectId()) && LMEvent.isStarted() || DMEvent.isPlayerParticipant(activeChar.getObjectId()) && DMEvent.isStarted() || KTBEvent.isPlayerParticipant(activeChar.getObjectId()) && KTBEvent.isStarted() || activeChar.isArenaAttack() || activeChar.isArenaProtection() || activeChar.isInsideZone(ZoneId.ARENA_EVENT) || activeChar.isInsideZone(ZoneId.FLAG_AREA_BOSS) || activeChar.isInsideZone(ZoneId.SIEGE) || FOSEvent.isPlayerParticipant(activeChar.getObjectId()) && FOSEvent.isStarted() || activeChar.isInsideZone(ZoneId.PVP_CUSTOM)  || activeChar.isInJail())
		{
			activeChar.sendMessage("You can not Action NOW.");
			return;
		}
		
		BookTeleport(activeChar);
	}
	public static void BookTeleport(Player activeChar)
	{
		if (activeChar.isInOlympiadMode() || activeChar.getPvpFlag() > 0 || activeChar.isMoving() || activeChar.isDead() || AttackStanceTaskManager.getInstance().isInAttackStance(activeChar) || activeChar.isCursedWeaponEquipped() || activeChar.isInArenaEvent() || OlympiadManager.getInstance().isRegistered(activeChar) || activeChar.getKarma() > 0 || activeChar.inObserverMode() || CTFEvent.isPlayerParticipant(activeChar.getObjectId()) && CTFEvent.isStarted() || TvTEvent.isPlayerParticipant(activeChar.getObjectId()) && TvTEvent.isStarted() || LMEvent.isPlayerParticipant(activeChar.getObjectId()) && LMEvent.isStarted() || DMEvent.isPlayerParticipant(activeChar.getObjectId()) && DMEvent.isStarted() || KTBEvent.isPlayerParticipant(activeChar.getObjectId()) && KTBEvent.isStarted() || activeChar.isArenaAttack() || activeChar.isArenaProtection() || activeChar.isInsideZone(ZoneId.ARENA_EVENT) || activeChar.isInsideZone(ZoneId.FLAG_AREA_BOSS) || activeChar.isInsideZone(ZoneId.SIEGE) || FOSEvent.isPlayerParticipant(activeChar.getObjectId()) && FOSEvent.isStarted() || activeChar.isInsideZone(ZoneId.PVP_CUSTOM)  || activeChar.isInJail())
		{
			activeChar.sendMessage("You can not Action NOW.");
			return;
		}
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/BookTeleport/Main.htm");
		if(Config.SOLO_FARM_BY_TIME_OF_DAY)
		{
			if(RewardSoloEvent.is_started())
				html.replace("%solofarm%", "In Progress");
			else	
				html.replace("%solofarm%", InitialSoloEvent.getInstance().getRestartNextTime().toString() );
		}
		if(Config.CHAMPION_FARM_BY_TIME_OF_DAY)
		{
			if(ChampionInvade.is_started())
				html.replace("%champion%", "In Progress");
			else	
				html.replace("%champion%", InitialChampionInvade.getInstance().getRestartNextTime().toString() );
		}
		if(Config.SOLO_BOSS_EVENT)
		{
			if(SoloBoss.is_started())
				html.replace("%bossSolo%", "In Progress");
			else	
				html.replace("%bossSolo%", InitialSoloBossEvent.getInstance().getRestartNextTime().toString() );
		}
		html.replace("%lmTime%", CheckNextEvent.getInstance().getNextLMTime());	
		
		//BossEvent na html EventsTime.
		if(KTBConfig.KTB_EVENT_ENABLED)
		{
			if(KTBEvent.isStarted())	
				html.replace("%ktbTime%", "In Progress");
			else
				html.replace("%ktbTime%", KTBManager.getInstance().getNextTime());
		}
		if(FOSConfig.FOS_EVENT_ENABLED)
		{
			if(FOSEvent.isStarted())	
				html.replace("%FOSTime%", "In Progress");
			else
				html.replace("%FOSTime%", FOSManager.getInstance().getNextTime());
		}
		if(DMConfig.DM_EVENT_ENABLED)
		{
			if(DMEvent.isStarted())	
				html.replace("%DMTime%", "In Progress");
			else
				html.replace("%DMTime%", DMManager.getInstance().getNextTime());
		}
		if(TvTConfig.TVT_EVENT_ENABLED)
		{
			if (TvTEvent.isStarted())
			{
				html.replace("%tvt%", "In Progress");
			}
			else
			{
				html.replace("%tvt%", CheckNextEvent.getInstance().getNextTvTTime());
			}
		}
		
		if(CTFConfig.CTF_EVENT_ENABLED)
		{
			if (CTFEvent.isStarted())
			{
				html.replace("%ctf%", "In Progress");
			}
			else
			{
				html.replace("%ctf%", CheckNextEvent.getInstance().getNextCTFTime());
			}
		}
		
		//Spoil Event EventsTime.
		if(Config.START_SPOIL)
		{
			if(SpoilEvent.is_started())	
				html.replace("%spoilevent%", "In Progress");
			else	
				html.replace("%spoilevent%", InitialSpoilEvent.getInstance().getRestartNextTime().toString() );
		}
		
		//Party farm EventsTime.
		if(Config.START_PARTY)
		{
			if(PartyFarm.is_started())	
				html.replace("%partyfarm%", "In Progress");
			else	
				html.replace("%partyfarm%", InitialPartyFarm.getInstance().getRestartNextTime().toString() );
		}
		if(Config.PVP_EVENT_ENABLED)
		{
			if(PvPEvent.getInstance().isActive())	
				html.replace("%pvp%", "In Progress");
			else	
				html.replace("%pvp%", PvPEventNext.getInstance().getNextTime().toString() );
		}
//		//MISSION Na htmTime.
		if(Config.ACTIVE_MISSION)
		{
			html.replace("%mission%", MissionReset.getInstance().getNextTime().toString() );
		}	
		
//		//CTF na html EventsTime.
	
//		//Tournament na html EventsTime.
		if(ArenaConfig.TOURNAMENT_EVENT_TIME)
		{
			if(ArenaTask.is_started())	
				html.replace("%arena%", "In Progress");
			else	
				html.replace("%arena%", ArenaEvent.getInstance().getNextTime().toString() );
		}
		activeChar.sendPacket(html);
	}
}
