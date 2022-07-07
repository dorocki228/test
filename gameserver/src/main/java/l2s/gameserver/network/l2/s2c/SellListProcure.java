package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.manor.CropProcure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellListProcure extends L2GameServerPacket
{
	private final long _money;
	private final Map<ItemInstance, Long> _sellList;
	private List<CropProcure> _procureList;
	private final int _castle;

	public SellListProcure(Player player, int castleId)
	{
		_sellList = new HashMap<>();
		_procureList = new ArrayList<>();
		_money = player.getAdena();
		_castle = castleId;
		_procureList = ResidenceHolder.getInstance().getResidence(Castle.class, _castle).getCropProcure(0);
		for(CropProcure c : _procureList)
		{
			ItemInstance item = player.getInventory().getItemByItemId(c.getId());
			if(item != null && c.getAmount() > 0L)
				_sellList.put(item, c.getAmount());
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeQ(_money);
        writeD(0);
        writeH(_sellList.size());
		for(ItemInstance item : _sellList.keySet())
		{
            writeH(item.getTemplate().getType1());
            writeD(item.getObjectId());
            writeD(item.getItemId());
			writeQ(_sellList.get(item));
            writeH(item.getTemplate().getType2());
            writeH(0);
			writeQ(0L);
		}
	}
}
