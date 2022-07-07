package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.model.items.Warehouse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrivateStoreBuyManageList extends L2GameServerPacket
{
	private final int _buyerId;
	private final long _adena;
	private final List<TradeItem> _buyList0;
	private final List<TradeItem> _buyList;

	public PrivateStoreBuyManageList(Player buyer)
	{
		if(!buyer.isPhantom()&& buyer.tScheme_record.isLogging())
			buyer.tScheme_record.setCheckTrader();
		
		_buyerId = buyer.getObjectId();
		_adena = buyer.getAdena();
		_buyList0 = buyer.getBuyList();
		_buyList = new ArrayList<>();
		ItemInstance[] items = buyer.getInventory().getItems();
		Arrays.sort(items, Warehouse.ItemClassComparator.getInstance());
		for(ItemInstance item : items)
			if(item.canBePrivateStore(buyer) && item.getItemId() != 57)
			{
				TradeItem bi;
				_buyList.add(bi = new TradeItem(item, item.getTemplate().isBlocked(buyer, item)));
				bi.setObjectId(0);
			}
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_buyerId);
		writeQ(_adena);
        writeD(_buyList.size());
		for(TradeItem bi : _buyList)
		{
            writeItemInfo(bi);
			writeQ(bi.getStorePrice());
		}
        writeD(_buyList0.size());
		for(TradeItem bi : _buyList0)
		{
            writeItemInfo(bi);
			writeQ(bi.getOwnersPrice());
			writeQ(bi.getStorePrice());
			writeQ(bi.getCount());
		}
	}
}
