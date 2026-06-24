package net.sf.l2j.gameserver.model.zone.type;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.instancemanager.custom.FarmZoneManager;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.L2SpawnZone;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;

/**
 * @author Sarada
 *
 */
public class L2FarmHwidZone extends L2SpawnZone
{
	private boolean _flagzone;
	private boolean _checkClasses;
	private static List<String> _classes = new ArrayList<>();
	public L2FarmHwidZone(int id)
	{
		super(id);
		_checkClasses = false;
		_flagzone = false;
	}
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("checkClasses"))
			_checkClasses = Boolean.parseBoolean(value);
		else if (name.equals("BlockClasses"))
		{
			_classes.clear(); // Limpa se recarregar a zona
			String[] propertySplit = value.split(",");
			for (String i : propertySplit)
				_classes.add(i.trim()); // .trim() evita espaços desnecessários
		}
		else if (name.equals("EnabledFlag"))
			_flagzone = Boolean.parseBoolean(value);
		else
		{
			super.setParameter(name, value);
		}
	}
	@Override
	protected void onEnter(Creature character)
	{
		if (character instanceof Player)
		{
			
			final Player player = (Player) character;
			if (_checkClasses)
			{
				String classIdStr = String.valueOf(player.getClassId().getId());
				if (!player.isGM() && _classes.contains(classIdStr))
				{
					ThreadPool.schedule(() -> {
						character.teleToLocation(83597, 147888, -3405, 0);
						character.sendMessage("Your class is not allowed in this zone.");
						player.broadcastUserInfo();
					}, 2000); // Delay de 2 segundos
				}
			}


			character.setInsideZone(ZoneId.FARM_HWID, true);
			character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
		
			if(_flagzone)
			{
				player.setInsideZone(ZoneId.FARM_HWID_FLAG, true);
				player.updatePvPFlag(1);
				player.broadcastUserInfo();
			}
			
			if (Config.BLOCK_DUALBOX_FARMZONE)
			{
				ThreadPool.schedule(() -> {
					MaxPlayersOnArea(player);
				}, 2000); // 2000 milissegundos = 2 segundos
			}

		}
	}
	
	public boolean MaxPlayersOnArea(Player activeChar)
	{
		if (activeChar.isGM())
			return true; // GMs não são contados
		return FarmZoneManager.getInstance().checkPlayersArea(activeChar, Config.DUALBOX_NUMBER_FARM, true);
	}
	
	@Override
	protected void onExit(Creature character)
	{
		if (character instanceof Player)
		{
			final Player player = (Player) character;
			character.setInsideZone(ZoneId.FARM_HWID, false);
			character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
			player.broadcastUserInfo();
			
			if(_flagzone)
			{
				player.setInsideZone(ZoneId.FARM_HWID_FLAG, false);
				player.updatePvPFlag(0);
				player.broadcastUserInfo();
				PvpFlagTaskManager.getInstance().add(player, Config.PVP_NORMAL_TIME);
			}
		
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