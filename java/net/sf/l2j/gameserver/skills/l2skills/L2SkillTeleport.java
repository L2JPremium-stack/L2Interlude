package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.event.bossevent.KTBEvent;
import net.sf.l2j.event.ctf.CTFEvent;
import net.sf.l2j.event.deathmath.DMEvent;
import net.sf.l2j.event.fortress.FOSEvent;
import net.sf.l2j.event.lastman.LMEvent;
import net.sf.l2j.event.tvt.TvTEvent;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.ShotType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.type.L2BossZone;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.templates.skills.L2SkillType;

public class L2SkillTeleport extends L2Skill
{
	private final String _recallType;
	private final Location _loc;
	
	public L2SkillTeleport(StatsSet set)
	{
		super(set);
		
		_recallType = set.getString("recallType", "");
		String coords = set.getString("teleCoords", null);
		if (coords != null)
		{
			String[] valuesSplit = coords.split(",");
			_loc = new Location(Integer.parseInt(valuesSplit[0]), Integer.parseInt(valuesSplit[1]), Integer.parseInt(valuesSplit[2]));
		}
		else
			_loc = null;
	}
	
	@Override
	public void useSkill(Creature activeChar, L2Object[] targets)
	{
		if (activeChar instanceof Player)
		{
			// Check invalid states.
			//if (activeChar.isAfraid() || ((L2PcInstance) activeChar).isInOlympiadMode() || ZoneManager.getInstance().getZone(activeChar, L2BossZone.class) != null)
			if (activeChar.isAfraid() || ((Player) activeChar).isInOlympiadMode())
				return;
		}
		
		boolean bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);
		
		for (L2Object obj : targets)
		{
			if (!(obj instanceof Creature))
				continue;
			
			final Creature target = ((Creature) obj);
			
			if (target instanceof Player)
			{
				Player targetChar = (Player) target;
				
				// Check invalid states.
				if (targetChar.isFestivalParticipant() || targetChar.isInJail() || targetChar.isInDuel())
					continue;
				
				if (targetChar.isArenaProtection() || KTBEvent.isPlayerParticipant(targetChar.getObjectId()) && KTBEvent.isStarted() || LMEvent.isStarted() && LMEvent.isPlayerParticipant(targetChar.getObjectId()) || DMEvent.isStarted() && DMEvent.isPlayerParticipant(targetChar.getObjectId()) || FOSEvent.isStarted() && FOSEvent.isPlayerParticipant(targetChar.getObjectId()) || TvTEvent.isStarted() && TvTEvent.isPlayerParticipant(targetChar.getObjectId()) || CTFEvent.isStarted() && CTFEvent.isPlayerParticipant(targetChar.getObjectId()))
				{
					targetChar.sendMessage("You can't use escape skill in Event.");
					continue;
				}
				
				if (targetChar != activeChar)
				{
					if (targetChar.isInOlympiadMode())
						continue;
					
					if (ZoneManager.getInstance().getZone(targetChar, L2BossZone.class) != null)
						continue;
				}
			}
			
			Location loc = null;
			if (getSkillType() == L2SkillType.TELEPORT)
			{
				if (_loc != null)
				{
					if (!(target instanceof Player) || !target.isFlying())
						loc = _loc;
				}
			}
			else
			{
				if (_recallType.equalsIgnoreCase("Castle"))
					loc = MapRegionTable.getInstance().getTeleToLocation(target, TeleportWhereType.CASTLE);
				else if (_recallType.equalsIgnoreCase("ClanHall"))
					loc = MapRegionTable.getInstance().getTeleToLocation(target, TeleportWhereType.CLAN_HALL);
				else
					loc = MapRegionTable.getInstance().getTeleToLocation(target, TeleportWhereType.TOWN);
			}
			
			if (loc != null)
			{
				if (target instanceof Player)
					((Player) target).setIsIn7sDungeon(false);
				
				target.teleToLocation(loc, 20);
			}
		}
		
		activeChar.setChargedShot(bsps ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, isStaticReuse());
	}
}