package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.serverpackets.AbstractNpcInfo.NpcInfo;
import net.sf.l2j.gameserver.network.serverpackets.ServerObjectInfo;

public class L2WaterZone extends L2ZoneType
{
	public L2WaterZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		character.setInsideZone(ZoneId.WATER, true);
		
		if (character instanceof Player)
			((Player) character).broadcastUserInfo();
		else if (character instanceof L2Npc)
		{
			for (Player player : character.getKnownList().getKnownType(Player.class))
			{
				if (character.getMoveSpeed() == 0)
					player.sendPacket(new ServerObjectInfo((L2Npc) character, player));
				else
					player.sendPacket(new NpcInfo((L2Npc) character, player));
			}
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		character.setInsideZone(ZoneId.WATER, false);
		
		// TODO: update to only send speed status when that packet is known
		if (character instanceof Player)
			((Player) character).broadcastUserInfo();
		else if (character instanceof L2Npc)
		{
			for (Player player : character.getKnownList().getKnownType(Player.class))
			{
				if (character.getMoveSpeed() == 0)
					player.sendPacket(new ServerObjectInfo((L2Npc) character, player));
				else
					player.sendPacket(new NpcInfo((L2Npc) character, player));
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
	
	public int getWaterZ()
	{
		return getZone().getHighZ();
	}
}