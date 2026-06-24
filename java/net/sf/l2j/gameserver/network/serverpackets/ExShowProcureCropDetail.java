package net.sf.l2j.gameserver.network.serverpackets;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager.CropProcure;
import net.sf.l2j.gameserver.model.entity.Castle;

/**
 * format(packet 0xFE) ch dd [dddc] c - id h - sub id d - crop id d - size [ d - manor name d - buy residual d - buy price c - reward type ]
 * @author l3x
 */
public class ExShowProcureCropDetail extends L2GameServerPacket
{
	private final int _cropId;
	private final Map<Integer, CropProcure> _castleCrops;
	
	public ExShowProcureCropDetail(int cropId)
	{
		_cropId = cropId;
		_castleCrops = new HashMap<>();
		
		for (Castle c : CastleManager.getInstance().getCastles())
		{
			CropProcure cropItem = c.getCrop(_cropId, CastleManorManager.PERIOD_CURRENT);
			if (cropItem != null && cropItem.getAmount() > 0)
				_castleCrops.put(c.getCastleId(), cropItem);
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x22);
		
		writeD(_cropId);
		writeD(_castleCrops.size());
		
		for (Map.Entry<Integer, CropProcure> cropEntry : _castleCrops.entrySet())
		{
			final CropProcure crop = cropEntry.getValue();
			
			writeD(cropEntry.getKey());
			writeD(crop.getAmount());
			writeD(crop.getPrice());
			writeC(crop.getReward());
		}
	}
}