package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Warehouse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PackageSendableListPacket extends L2GameServerPacket
{
	private final int _targetObjectId;
	private final long _adena;
	private final List<ItemInfo> _itemList;

	public PackageSendableListPacket(int objectId, Player cha)
	{
		_adena = cha.getAdena();
		_targetObjectId = objectId;
		ItemInstance[] items = cha.getInventory().getItems();
		Arrays.sort(items, Warehouse.ItemClassComparator.getInstance());
		_itemList = new ArrayList<>(items.length);
		for(ItemInstance item : items)
			if(item.canBeFreighted(cha))
				_itemList.add(new ItemInfo(item, item.getTemplate().isBlocked(cha, item)));
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_targetObjectId);
		writeQ(_adena);
        writeD(_itemList.size());
		for(ItemInfo item : _itemList)
		{
            writeItemInfo(item);
            writeD(item.getObjectId());
		}
	}
}
