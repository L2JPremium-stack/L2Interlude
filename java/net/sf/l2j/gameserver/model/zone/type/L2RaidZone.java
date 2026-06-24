package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.instancemanager.custom.RaidZoneManager;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.L2SpawnZone;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;

/**
 * @author Sarada
 *
 */
public class L2RaidZone extends L2SpawnZone
{
	public L2RaidZone(int id)
	{
		super(id);
		_maxClanMembers = 0;
		_maxAllyMembers = 0;
		_minPartyMembers = 0;
		_checkParty = false;
		_checkClan = false;
		_checkAlly = false;
	}
	private boolean _checkLevel;
	private int _MaxLevel;
	private int _maxClanMembers;
	private int _maxAllyMembers;
	private int _minPartyMembers;
	private boolean _checkParty;
	private boolean _checkClan;
	private boolean _checkAlly;
	@Override
	public void setParameter(String name, String value)
	{
		 if (name.equals("EnableLevelLimite"))
			_checkLevel = Boolean.parseBoolean(value);
		else if (name.equals("LimiteLv"))
		{
			_MaxLevel = Integer.parseInt(value);
		}
		else if (name.equals("MaxClanMembers"))
			_maxClanMembers = Integer.parseInt(value);
		else if (name.equals("MaxAllyMembers"))
			_maxAllyMembers = Integer.parseInt(value);
		else if (name.equals("MinPartyMembers"))
			_minPartyMembers = Integer.parseInt(value);
		else if (name.equals("checkParty"))
			_checkParty = Boolean.parseBoolean(value);
		else if (name.equals("checkClan"))
			_checkClan = Boolean.parseBoolean(value);
		else if (name.equals("checkAlly"))
			_checkAlly = Boolean.parseBoolean(value);
		else
			super.setParameter(name, value);
	}
	@Override
	protected void onEnter(Creature character)
	{
		
		if (character instanceof Player)
		{
			final Player player = (Player) character;
			character.setInsideZone(ZoneId.RAID_ZONE, true);
			character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
			player.updatePvPFlag(1);
			player.broadcastUserInfo();
			player.sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
			
			ThreadPool.schedule(() -> {
			    if (Config.BLOCK_DUALBOX_RAIDZONE)
			    {
			    	MaxPlayersOnArea(player, this);
			    }
			}, 2000); // Delay de 2 segundos

			ThreadPool.schedule(() -> {
			    if (player.isGM())
			        return;
			    
			    // Coloque aqui o restante da lógica que deve ser executada APÓS a verificação do GM
			}, 2000); // Delay de 2 segundos

			
			ThreadPool.schedule(() -> {
				if (_checkParty && _minPartyMembers > 0 && !player.isGM())
				{
					if (!player.isInParty() || player.getParty().getMemberCount() < _minPartyMembers)
					{
						player.sendPacket(new ExShowScreenMessage("Your party does not have " + _minPartyMembers + " members to enter on this zone!", 6 * 1000));
						RaidZoneManager.getInstance().RandomTeleport(player);
					}
				}
			}, 2000); // Delay de 2 segundos

			if (_checkClan)
			{
				ThreadPool.schedule(() -> {
					MaxClanMembersOnArea(player, this);
				}, 2000); // Delay de 2 segundos
			}


			if (_checkAlly)
			{
				ThreadPool.schedule(() -> {
					MaxAllyMembersOnArea(player, this);
				}, 2000); // Delay de 2 segundos
			}

			
			
			ThreadPool.schedule(() -> {
				if (player.getLevel() > _MaxLevel && _checkLevel)
				{
					player.sendMessage("Limite Zone, Max Lv " + _MaxLevel);
					player.teleToLocation(82488, 149064, -3464, 0);
					// return aqui dentro é opcional se não tiver mais nada depois
				}
			}, 2000); // Delay de 2 segundos

		}
		
	}
	public boolean MaxPlayersOnArea(Player activeChar, L2RaidZone zone)
	{
		return RaidZoneManager.getInstance().checkPlayersArea(activeChar, zone, 1, true);
	}

	public boolean MaxClanMembersOnArea(Player activeChar, L2RaidZone zone)
	{
		return RaidZoneManager.getInstance().checkClanArea(activeChar, zone, _maxClanMembers, true);
	}
	public boolean MaxAllyMembersOnArea(Player activeChar, L2RaidZone zone)
	{
		return RaidZoneManager.getInstance().checkAllyArea(activeChar, zone, _maxAllyMembers, L2World.getInstance().getPlayers(), true);
	}


	@Override
	protected void onExit(Creature character)
	{
		if (character instanceof Player)
		{
			final Player activeChar = (Player) character;
			character.setInsideZone(ZoneId.RAID_ZONE, false);
			character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
			activeChar.updatePvPFlag(0);
			activeChar.broadcastUserInfo();
			PvpFlagTaskManager.getInstance().add(activeChar, Config.PVP_NORMAL_TIME);
			character.sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
		}
	}
	@Override
	public void onDieInside(Creature character)
	{
		
	}
	@Override
	public void onReviveInside(Creature character)
	{
		
	}
	
}
