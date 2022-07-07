package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.TradeItem;

import java.util.ArrayList;
import java.util.List;

public class PrivateStoreBuyList extends L2GameServerPacket
{
	private final int _buyerId;
	private final long _adena;
	private final List<TradeItem> _sellList;

	public PrivateStoreBuyList(Player seller, Player buyer)
	{
		if(!buyer.isPhantom()&& buyer.tScheme_record.isLogging())
			buyer.tScheme_record.setCheckTrader();
		
		_adena = seller.getAdena();
		_buyerId = buyer.getObjectId();
		_sellList = new ArrayList<>();
		List<TradeItem> buyList = buyer.getBuyList();
		ItemInstance[] items = seller.getInventory().getItems();
		for(TradeItem bi : buyList)
		{
			TradeItem si = null;
			for(ItemInstance item : items)
				if(item.getItemId() == bi.getItemId() && item.canBePrivateStore(seller))
				{
					si = new TradeItem(item);
					_sellList.add(si);
					si.setOwnersPrice(bi.getOwnersPrice());
					si.setCount(bi.getCount());
					si.setCurrentValue(Math.min(bi.getCount(), item.getCount()));
				}

			if(si == null)
			{
				si = new TradeItem();
				si.setItemId(bi.getItemId());
				si.setOwnersPrice(bi.getOwnersPrice());
				si.setCount(bi.getCount());
				si.setCurrentValue(0L);
				_sellList.add(si);
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_buyerId);
		writeQ(_adena);
		writeD(70);
		writeD(_sellList.size());
		for(TradeItem si : _sellList)
		{
			writeItemInfo(si, si.getCurrentValue());
			writeD(si.getObjectId());
			writeQ(si.getOwnersPrice());
			writeQ(si.getStorePrice());
			writeQ(si.getCount());
		}
	}
}
