package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;

/**
 * Format: ch ddcdc
 * @author KenM
 */
/*public class ExPCCafePointInfo extends L2GameServerPacket
{
	private final int _score, _modify, _periodType, _remainingTime;
	private int _pointType = 0;
	
	public ExPCCafePointInfo(int score, int modify, boolean addPoint, boolean pointType, int remainingTime)
	{
		_score = score;
		_modify = addPoint ? modify : modify * -1;
		_remainingTime = remainingTime;
		_pointType = addPoint ? (pointType ? 0 : 1) : 2;
		_periodType = 1; // get point time
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x31);
		writeD(_score);
		writeD(_modify);
		writeC(_periodType);
		writeD(_remainingTime);
		writeC(_pointType);
	}*/
public class ExPCCafePointInfo extends L2GameServerPacket
{
	private Player _character;
	private int m_AddPoint;
	private int m_PeriodType;
	private int RemainTime;
	private int PointType;
	
	public ExPCCafePointInfo(Player user, int modify, boolean add, int hour, boolean _double)
	{
		_character = user;
		m_AddPoint = modify;
		
		if (add)
		{
			m_PeriodType = 1;
			PointType = 1;
		}
		else
		{
			if (add && _double)
			{
				m_PeriodType = 1;
				PointType = 0;
			}
			else
			{
				m_PeriodType = 2;
				PointType = 2;
			}
		}
		
		RemainTime = hour;
		
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x31);
		writeD(_character.getPcBangScore());
		writeD(m_AddPoint);
		writeC(m_PeriodType);
		writeD(RemainTime);
		writeC(PointType);
	}
}