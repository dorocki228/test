package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.TradeItem;

import java.util.ArrayList;
import java.util.List;

public class PrivateStoreManageList extends L2GameServerPacket
{
	private final int _sellerId;
	private final long _adena;
	private final boolean _package;
	private final List<TradeItem> _sellList;
	private final List<TradeItem> _sellList0;

	public PrivateStoreManageList(Player seller, boolean pkg)
	{
		_sellerId = seller.getObjectId();
		_adena = seller.getAdena();
		_package = pkg;
		_sellList0 = seller.getSellList(_package);
		_sellList = new ArrayList<>();
		for(TradeItem si : _sellList0)
			if(si.getCount() <= 0L)
				_sellList0.remove(si);
			else
			{
				ItemInstance item = seller.getInventory().getItemByObjectId(si.getObjectId());
				if(item == null)
					item = seller.getInventory().getItemByItemId(si.getItemId());
				if(item == null || !item.canBePrivateStore(seller) || item.getItemId() == 57)
					_sellList0.remove(si);
				else
					si.setCount(Math.min(item.getCount(), si.getCount()));
			}
		ItemInstance[] items2;
		ItemInstance[] items = items2 = seller.getInventory().getItems();
		for(ItemInstance item2 : items2)
			Label_0397:
			{
				if(item2.canBePrivateStore(seller) && item2.getItemId() != 57)
				{
					for(TradeItem si2 : _sellList0)
						if(si2.getObjectId() == item2.getObjectId())
						{
							if(si2.getCount() == item2.getCount())
								break Label_0397;
							TradeItem ti = new TradeItem(item2, item2.getTemplate().isBlocked(seller, item2));
							ti.setCount(item2.getCount() - si2.getCount());
							_sellList.add(ti);
							break Label_0397;
						}
					_sellList.add(new TradeItem(item2, item2.getTemplate().isBlocked(seller, item2)));
				}
			}
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_sellerId);
        writeD(_package ? 1 : 0);
		writeQ(_adena);
        writeD(_sellList.size());
		for(TradeItem si : _sellList)
		{
            writeItemInfo(si);
			writeQ(si.getStorePrice());
		}
        writeD(_sellList0.size());
		for(TradeItem si : _sellList0)
		{
            writeItemInfo(si);
			writeQ(si.getOwnersPrice());
			writeQ(si.getStorePrice());
		}
	}
}
