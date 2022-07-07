package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.templates.manor.CropProcure;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ExShowProcureCropDetail extends L2GameServerPacket
{
	private final int _cropId;
	private final Map<Integer, CropProcure> _castleCrops;

	public ExShowProcureCropDetail(int cropId)
	{
		_cropId = cropId;
		_castleCrops = new TreeMap<>();
		List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);
		for(Castle c : castleList)
		{
			CropProcure cropItem = c.getCrop(_cropId, 0);
			if(cropItem != null && cropItem.getAmount() > 0L)
				_castleCrops.put(c.getId(), cropItem);
		}
	}

	@Override
	public void writeImpl()
	{
        writeD(_cropId);
        writeD(_castleCrops.size());
		for(int manorId : _castleCrops.keySet())
		{
			CropProcure crop = _castleCrops.get(manorId);
            writeD(manorId);
			writeQ(crop.getAmount());
			writeQ(crop.getPrice());
            writeC(crop.getReward());
		}
	}
}
