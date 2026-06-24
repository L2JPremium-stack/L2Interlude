package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.L2SpawnZone;
import net.sf.l2j.gameserver.model.zone.ZoneId;

public class L2VipZone extends L2SpawnZone
{
	public L2VipZone(int id)
	{
		super(id);
	}
	@Override
	protected void onEnter(Creature character)
	{

		if (character instanceof Player)
		{
			final Player player = (Player) character;
			
			if (!player.isVip())
			{
				character.teleToLocation(83597, 147888, -3405, 0);
				character.sendMessage("You Need Vip Status for Entrance Zone!.");
				player.broadcastUserInfo();
				return;
			}
				
			character.setInsideZone(ZoneId.VIP_ZONE, true);
			character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
			player.broadcastUserInfo();
			character.sendMessage("You Entrance to Vip Zone!");
			return;
		}
	}

	@Override
	protected void onExit(Creature character)
	{
		if (character instanceof Player)
		{
			final Player activeChar = (Player) character;
			character.setInsideZone(ZoneId.VIP_ZONE, false);
			character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
			character.sendMessage("You Leave to Vip Zone!");
			activeChar.broadcastUserInfo();
			return;
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