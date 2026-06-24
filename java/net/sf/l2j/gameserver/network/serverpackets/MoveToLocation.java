package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Creature;

/**
 * Sent by the server to broadcast a creature movement destination.
 *
 * Coordinates are captured at construction time to keep the packet consistent
 * even if the creature position changes before writeImpl() is executed.
 */
public final class MoveToLocation extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _xDst;
	private final int _yDst;
	private final int _zDst;
	
	public MoveToLocation(Creature cha)
	{
		_charObjId = cha.getObjectId();
		
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		
		_xDst = cha.getXdestination();
		_yDst = cha.getYdestination();
		_zDst = cha.getZdestination();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x01);
		writeD(_charObjId);
		
		writeD(_xDst);
		writeD(_yDst);
		writeD(_zDst);
		
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
