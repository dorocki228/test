package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.LockType;

public class ExQuestItemListPacket extends L2GameServerPacket
{
	private final int _size;
	private final ItemInstance[] _items;
	private final LockType _lockType;
	private final int[] _lockItems;

	public ExQuestItemListPacket(int size, ItemInstance[] t, LockType lockType, int[] lockItems)
	{
		_size = size;
		_items = t;
		_lockType = lockType;
		_lockItems = lockItems;
	}

	@Override
	protected void writeImpl()
	{
        writeH(_size);
		for(ItemInstance temp : _items)
			if(temp.getTemplate().isQuest())
                writeItemInfo(temp);
        writeH(_lockItems.length);
		if(_lockItems.length > 0)
		{
            writeC(_lockType.ordinal());
			for(int i : _lockItems)
                writeD(i);
		}
	}
}
