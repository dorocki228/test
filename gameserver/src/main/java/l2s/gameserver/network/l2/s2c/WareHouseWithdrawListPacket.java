package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Warehouse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WareHouseWithdrawListPacket extends L2GameServerPacket
{
	private final long _adena;
	private List<ItemInfo> _itemList;
	private final int _type;
	private int _inventoryUsedSlots;

	public WareHouseWithdrawListPacket(Player player, Warehouse.WarehouseType type)
	{
		_itemList = new ArrayList<>();
		_adena = player.getAdena();
		_type = type.ordinal();
		ItemInstance[] items = null;
		switch(type)
		{
			case PRIVATE:
			{
				items = player.getWarehouse().getItems();
				break;
			}
			case FREIGHT:
			{
				items = player.getFreight().getItems();
				break;
			}
			case CLAN:
			case CASTLE:
			{
				items = player.getClan().getWarehouse().getItems();
				break;
			}
			default:
			{
				_itemList = Collections.emptyList();
				return;
			}
		}
		_itemList = new ArrayList<>(items.length);
		Arrays.sort(items, Warehouse.ItemClassComparator.getInstance());
		for(ItemInstance item : items)
			_itemList.add(new ItemInfo(item));
		_inventoryUsedSlots = player.getInventory().getSize();
	}

	@Override
	protected final void writeImpl()
	{
        writeH(_type);
		writeQ(_adena);
        writeH(_itemList.size());
		if(_type == 1 || _type == 2)
			if(!_itemList.isEmpty())
			{
                writeH(1);
                writeD(4195);
			}
			else
                writeH(0);
        writeD(_inventoryUsedSlots);
		for(ItemInfo item : _itemList)
		{
            writeItemInfo(item);
            writeD(item.getObjectId());
            writeD(0);
            writeD(0);
		}
	}
}
