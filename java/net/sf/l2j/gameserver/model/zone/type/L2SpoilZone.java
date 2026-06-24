package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.Config;
import net.sf.l2j.AutoFarm.AutofarmManager;
import net.sf.l2j.gameserver.handler.tutorialhandlers.Autofarm;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.zone.L2SpawnZone;
import net.sf.l2j.gameserver.model.zone.ZoneId;

/**
 * @author Sarada
 *
 */
public class L2SpoilZone extends L2SpawnZone
{

	public L2SpoilZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(Creature character)
	{
		if (character instanceof Player)
		{
			final Player player = (Player) character;
			
			if(player.isGM())
			{
				return;
			}
			if(player.isInsideZone(ZoneId.SPOIL_AREA) && Config.BLOCK_AUTOFARM_SPOILZONE)
			{
				player.sendMessage("You don't events to use autofarm.");
				player.broadcastUserInfo();
				AutofarmManager.INSTANCE.stopFarm(player);
				player.setAutoFarm(false);
				Autofarm.showAutoFarm(player);
				player.broadcastUserInfo();
				return;
			}
			if(!(player.getClassId() == ClassId.SCAVENGER) && !(player.getClassId() == ClassId.BOUNTY_HUNTER) && !(player.getClassId() == ClassId.FORTUNE_SEEKER))
			{
				player.sendMessage("Only Class Spoil!");
				player.teleToLocation(82488,149064,-3464,0);
				return;
			}
			
			player.sendMessage("You entered in Spoil Zone!");
			player.setInsideZone(ZoneId.SPOIL_AREA, true);
			return;
		}
	}

	@Override
	protected void onExit(Creature character)
	{
		if (character instanceof Player)
		{
			final Player activeChar = (Player) character;
			activeChar.sendMessage("You exit in Spoil Zone!");
			activeChar.setInsideZone(ZoneId.SPOIL_AREA, false);
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
