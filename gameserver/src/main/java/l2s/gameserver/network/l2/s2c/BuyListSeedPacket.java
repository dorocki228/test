package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.templates.npc.BuyListTemplate;

import java.util.ArrayList;
import java.util.List;

public final class BuyListSeedPacket extends L2GameServerPacket
{
	private final int _manorId;
	private List<TradeItem> _list;
	private final long _money;

	public BuyListSeedPacket(BuyListTemplate list, int manorId, long currentMoney)
	{
		_list = new ArrayList<>();
		_money = currentMoney;
		_manorId = manorId;
		_list = list.getItems();
	}

	@Override
	protected final void writeImpl()
	{
		writeQ(_money);
        writeD(0);
        writeD(_manorId);
        writeH(_list.size());
		for(TradeItem item : _list)
		{
            writeItemInfo(item);
			writeQ(item.getOwnersPrice());
		}
	}
}
