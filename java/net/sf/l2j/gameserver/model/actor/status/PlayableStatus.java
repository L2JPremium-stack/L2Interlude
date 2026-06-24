package net.sf.l2j.gameserver.model.actor.status;

import net.sf.l2j.gameserver.model.actor.Playable;

public class PlayableStatus extends CharStatus
{
	public PlayableStatus(Playable activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public Playable getActiveChar()
	{
		return (Playable) super.getActiveChar();
	}
}