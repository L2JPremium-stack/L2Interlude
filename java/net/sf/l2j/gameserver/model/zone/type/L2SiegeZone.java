package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeSummonInstance;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;

/**
 * A siege zone
 * @author durgus
 */
public class L2SiegeZone extends L2ZoneType
{
	private int _siegableId = -1;
	private boolean _isActiveSiege = false;
	private static final int DISMOUNT_DELAY = 5;
	
	public L2SiegeZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("castleId"))
		{
			if (_siegableId != -1)
				throw new IllegalArgumentException("Siege object already defined!");
			_siegableId = Integer.parseInt(value);
		}
		else if (name.equals("clanHallId"))
		{
			if (_siegableId != -1)
				throw new IllegalArgumentException("Siege object already defined!");
			_siegableId = Integer.parseInt(value);
		}
		else
			super.setParameter(name, value);
	}
	
	@Override
	/*protected void onEnter(L2Character character)
	{
		if (_isActiveSiege)
		{
			character.setInsideZone(ZoneId.PVP, true);
			character.setInsideZone(ZoneId.SIEGE, true);
			character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
			
			if (character instanceof L2PcInstance)
			{
				L2PcInstance activeChar = (L2PcInstance) character;
				
				activeChar.setIsInSiege(true); // in siege
				
				activeChar.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
				
				if (activeChar.getMountType() == 2)
				{
					activeChar.sendPacket(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN);
					activeChar.enteredNoLanding(DISMOUNT_DELAY);
				}
			}
		}
	}*/
	protected void onEnter(Creature character)
	{
		if (_isActiveSiege)
		{
			character.setInsideZone(ZoneId.PVP, true);
			character.setInsideZone(ZoneId.SIEGE, true);
			character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
			
			if (character instanceof Player)
			{
				Player activeChar = (Player) character;
				
				// Skip other checks for GM.
				if (character.isGM())
					return;
				
				if (Config.ENABLE_CLAN_REGS_SIEGE)
				if (activeChar.isRegisteredOnThisSiegeField(getSiegeObjectId()))
				{
					activeChar.setIsInSiege(true); // in siege
				}
				else
				{
						activeChar.teleToLocation(MapRegionTable.TeleportWhereType.TOWN);
						activeChar.sendMessage(Config.MESSEGE_CLAN_REGISTRADOS);
				}
				else
				{
					activeChar.setIsInSiege(true); // in siege
				}
				
				activeChar.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
				activeChar.enteredNoLanding(DISMOUNT_DELAY);
			}
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		character.setInsideZone(ZoneId.PVP, false);
		character.setInsideZone(ZoneId.SIEGE, false);
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
		
		if (character instanceof Player)
		{
			final Player activeChar = (Player) character;
			
			if (_isActiveSiege)
			{
				activeChar.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
				
				if (activeChar.getMountType() == 2)
					activeChar.exitedNoLanding();
				
				PvpFlagTaskManager.getInstance().add(activeChar, Config.PVP_NORMAL_TIME);
				
				// Set pvp flag
				if (activeChar.getPvpFlag() == 0)
					activeChar.updatePvPFlag(1);
			}
			
			activeChar.setIsInSiege(false);
		}
		else if (character instanceof L2SiegeSummonInstance)
			((L2SiegeSummonInstance) character).unSummon(((L2SiegeSummonInstance) character).getOwner());
	}
	
	@Override
	public void onDieInside(Creature character)
	{
	}
	
	@Override
	public void onReviveInside(Creature character)
	{
	}
	
	public void updateZoneStatusForCharactersInside()
	{
		if (_isActiveSiege)
		{
			for (Creature character : _characterList)
			{
				if (character != null)
					onEnter(character);
			}
		}
		else
		{
			for (Creature character : _characterList)
			{
				if (character == null)
					continue;
				
				character.setInsideZone(ZoneId.PVP, false);
				character.setInsideZone(ZoneId.SIEGE, false);
				character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
				
				if (character instanceof Player)
				{
					final Player player = ((Player) character);
					
					player.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
					
					if (player.getMountType() == 2)
						player.exitedNoLanding();
				}
				else if (character instanceof L2SiegeSummonInstance)
					((L2SiegeSummonInstance) character).unSummon(((L2SiegeSummonInstance) character).getOwner());
			}
		}
	}
	
	/**
	 * Sends a message to all players in this zone
	 * @param message
	 */
	public void announceToPlayers(String message)
	{
		for (Player player : getKnownTypeInside(Player.class))
			player.sendMessage(message);
	}
	
	public int getSiegeObjectId()
	{
		return _siegableId;
	}
	
	public boolean isActive()
	{
		return _isActiveSiege;
	}
	
	public void setIsActive(boolean val)
	{
		_isActiveSiege = val;
	}
	
	/**
	 * Removes all foreigners from the zone
	 * @param owningClan
	 */
	public void banishForeigners(L2Clan owningClan)
	{
		for (Player player : getKnownTypeInside(Player.class))
		{
			if (player.getClan() == owningClan || player.isGM())
				continue;
			
			player.teleToLocation(TeleportWhereType.TOWN);
		}
	}
}