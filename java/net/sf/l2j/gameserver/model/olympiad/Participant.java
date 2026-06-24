package net.sf.l2j.gameserver.model.olympiad;

import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.templates.StatsSet;

/**
 * @author DS
 */
public final class Participant
{
	public final int objectId;
	public Player player;
	public final String name;
	public final int side;
	public final int baseClass;
	public boolean disconnected = false;
	public boolean defaulted = false;
	public final StatsSet stats;
	
	public Participant(Player plr, int olympiadSide)
	{
		objectId = plr.getObjectId();
		player = plr;
		name = plr.getName();
		side = olympiadSide;
		baseClass = plr.getBaseClass();
		stats = Olympiad.getNobleStats(objectId);
	}
	
	public Participant(int objId, int olympiadSide)
	{
		objectId = objId;
		player = null;
		name = "-";
		side = olympiadSide;
		baseClass = 0;
		stats = null;
	}
	
	public final void updatePlayer()
	{
		if (player == null || !player.isOnline())
			player = L2World.getInstance().getPlayer(objectId);
	}
	
	/**
	 * Releases the strong Player reference kept by this participant.
	 * Olympiad keeps objectId/name/stats, so the Player instance must not be
	 * retained after a game is finished or aborted.
	 */
	public final void clear()
	{
		player = null;
	}
	
	public final void updateStat(String statName, int increment)
	{
		stats.set(statName, Math.max(stats.getInteger(statName) + increment, 0));
	}
}