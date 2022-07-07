package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Manor;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.manor.CropProcure;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ExShowSellCropListPacket extends L2GameServerPacket
{
	private int _manorId;
	private final Map<Integer, ItemInstance> _cropsItems;
	private final Map<Integer, CropProcure> _castleCrops;

	public ExShowSellCropListPacket(Player player, int manorId, List<CropProcure> crops)
	{
		_manorId = 1;
		_manorId = manorId;
		_castleCrops = new TreeMap<>();
		_cropsItems = new TreeMap<>();
		List<Integer> allCrops = Manor.getInstance().getAllCrops();
		for(int cropId : allCrops)
		{
			ItemInstance item = player.getInventory().getItemByItemId(cropId);
			if(item != null)
				_cropsItems.put(cropId, item);
		}
		for(CropProcure crop : crops)
			if(_cropsItems.containsKey(crop.getId()) && crop.getAmount() > 0L)
				_castleCrops.put(crop.getId(), crop);
	}

	@Override
	public void writeImpl()
	{
        writeD(_manorId);
        writeD(_cropsItems.size());
		for(ItemInstance item : _cropsItems.values())
		{
            writeD(item.getObjectId());
            writeD(item.getItemId());
            writeD(Manor.getInstance().getSeedLevelByCrop(item.getItemId()));
            writeC(1);
            writeD(Manor.getInstance().getRewardItem(item.getItemId(), 1));
            writeC(1);
            writeD(Manor.getInstance().getRewardItem(item.getItemId(), 2));
			if(_castleCrops.containsKey(item.getItemId()))
			{
				CropProcure crop = _castleCrops.get(item.getItemId());
                writeD(_manorId);
				writeQ(crop.getAmount());
				writeQ(crop.getPrice());
                writeC(crop.getReward());
			}
			else
			{
                writeD(-1);
				writeQ(0L);
				writeQ(0L);
                writeC(0);
			}
			writeQ(item.getCount());
		}
	}
}
