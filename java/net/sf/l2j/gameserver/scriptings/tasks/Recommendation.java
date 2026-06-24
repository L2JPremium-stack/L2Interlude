package net.sf.l2j.gameserver.scriptings.tasks;

import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.scriptings.ScheduledQuest;

/**
 * @author Hasha
 */
public final class Recommendation extends ScheduledQuest
{
	public Recommendation()
	{
		super(-1, "tasks");
	}
	
	@Override
	public final void onStart()
	{
		for (Player player : L2World.getInstance().getPlayers())
		{
			player.restartRecom();
			player.sendPacket(new UserInfo(player));
		}
		
		_log.config("Recommendation: Recommendation has been reset.");
	}
	
	@Override
	public final void onEnd()
	{
	}
}