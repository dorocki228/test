package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInfo;

public class ExChangeAttributeItemList extends L2GameServerPacket
{
	private final ItemInfo[] _itemsList;
	private final int _itemId;

	public ExChangeAttributeItemList(int itemId, ItemInfo[] itemsList)
	{
		_itemId = itemId;
		_itemsList = itemsList;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_itemId);
        writeD(_itemsList.length);
		for(ItemInfo item : _itemsList)
            writeItemInfo(item);
	}
}
