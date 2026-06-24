package net.sf.l2j.gameserver.model.zone.type;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.L2SpawnZone;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.taskmanager.PvPZoneTimeTask;
import net.sf.l2j.gameserver.taskmanager.PvPZoneTimeTask.TimeUtil;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;

public class L2FlagZone extends L2SpawnZone
{
	private boolean _checkClasses;
	private static List<String> _classes = new ArrayList<>();
	
	private int _id;
	private String _name;
	private final List<Location> _doReviveLocations = new ArrayList<>();
	private int _activeTime;
	private boolean _active;
	
	public L2FlagZone(int id)
	{
		super(id);
		_checkClasses = false;
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		
		switch (name)
		{
			case "id":
				_id = Integer.parseInt(value);
				break;
			
			case "name":
				_name = value;
				break;
			
			case "activeTime":
				_activeTime = Integer.parseInt(value);
				break;
			
			case "reviveLoc":
				for (String locs : value.split(";"))
				{
					String[] parts = locs.split(",");
					_doReviveLocations.add(new Location(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
				}
				break;
			case "checkClasses":
				_checkClasses = Boolean.parseBoolean(value);
				break;
			
			case "BlockClasses":
				String[] propertySplit = value.split(",");
				for (String i : propertySplit)
					_classes.add(i);
				break;
			
			default:
				super.setParameter(name, value);
		}
		
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		
		if (character instanceof Player)
		{
			final Player player = (Player) character;
			
			if (isActive())
			{
				
				if (_checkClasses)
				{
					String classIdStr = String.valueOf(player.getClassId().getId());
					if (!player.isGM() && _classes.contains(classIdStr))
					{
						player.setIsImmobilized(true);
						ThreadPool.schedule(() -> {
							player.teleToLocation(83597, 147888, -3405, 0);
							player.sendMessage("Your class is not allowed in this zone.");
							player.broadcastUserInfo();
							player.setIsImmobilized(false);
						}, 2000);
						return;
					}
				}
				
				PvPZoneTimeTask task = PvPZoneTimeTask.getInstance();
				int timeLeft = task.getTimeLeft();
				player.sendMessage("Atenção: você entrou na PvP Zone " + getName() + ". Troca em " + TimeUtil.formatSeconds(timeLeft) + ".");
				
				player.setInsideZone(ZoneId.FLAG, true);
				player.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
				player.updatePvPFlag(1);
				player.broadcastUserInfo();
			}
			
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		if (character instanceof Player)
		{
			final Player activeChar = (Player) character;
			
			if (isActive())
			{
				Location loc = getRandomReviveLoc();
				if (loc != null)
					activeChar.teleToLocation(loc, 75);
				return;
			}
			
			activeChar.setInsideZone(ZoneId.FLAG, false);
			activeChar.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
			activeChar.updatePvPFlag(0);
			activeChar.broadcastUserInfo();
			PvpFlagTaskManager.getInstance().add(activeChar, Config.PVP_NORMAL_TIME);
			
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
	
	@Override
	public int getId()
	{
		return _id;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getActiveTime()
	{
		return _activeTime;
	}
	
	public boolean isActive()
	{
		return _active;
	}
	
	public void setActive(boolean active)
	{
		_active = active;
	}
	
	public Location getRandomReviveLoc()
	{
		if (_doReviveLocations.isEmpty())
			return null;
		
		return _doReviveLocations.get(ThreadLocalRandom.current().nextInt(_doReviveLocations.size()));
	}
}