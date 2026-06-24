package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.taskmanager.GameTimeController;

public class ClientSetTime extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(0xEC);
		writeD(GameTimeController.getInstance().getGameTime());
		writeD(6);
	}
}