package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Warehouse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WareHouseDepositListPacket extends L2GameServerPacket
{
	private final int _whtype;
	private final long _adena;
	private final List<ItemInfo> _itemList;
	private final int _depositedItemsCount;

	public WareHouseDepositListPacket(Player cha, Warehouse.WarehouseType whtype)
	{
		_whtype = whtype.ordinal();
		_adena = cha.getAdena();
		ItemInstance[] items = cha.getInventory().getItems();
		Arrays.sort(items, Warehouse.ItemClassComparator.getInstance());
		_itemList = new ArrayList<>(items.length);
		for(ItemInstance item : items)
			if(item.canBeStored(cha, _whtype == 1))
				_itemList.add(new ItemInfo(item, item.getTemplate().isBlocked(cha, item)));
		switch(whtype)
		{
			case PRIVATE:
			{
				_depositedItemsCount = cha.getWarehouse().getSize();
				break;
			}
			case FREIGHT:
			{
				_depositedItemsCount = cha.getFreight().getSize();
				break;
			}
			case CLAN:
			case CASTLE:
			{
				_depositedItemsCount = cha.getClan().getWarehouse().getSize();
				break;
			}
			default:
			{
				_depositedItemsCount = 0;
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
        writeH(_whtype);
		writeQ(_adena);
        writeH(_depositedItemsCount);
        writeD(0);
        writeH(_itemList.size());
		for(ItemInfo item : _itemList)
		{
            writeItemInfo(item);
            writeD(item.getObjectId());
		}
	}
}
