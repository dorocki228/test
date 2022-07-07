package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;

import java.util.ArrayList;
import java.util.List;

public class PetInventoryUpdatePacket extends L2GameServerPacket
{
	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int MODIFIED = 2;
	public static final int REMOVED = 3;
	private final List<ItemInfo> _items;

	public PetInventoryUpdatePacket()
	{
		_items = new ArrayList<>(1);
	}

	public PetInventoryUpdatePacket addNewItem(ItemInstance item)
	{
		addItem(item).setLastChange(1);
		return this;
	}

	public PetInventoryUpdatePacket addModifiedItem(ItemInstance item)
	{
		addItem(item).setLastChange(2);
		return this;
	}

	public PetInventoryUpdatePacket addRemovedItem(ItemInstance item)
	{
		addItem(item).setLastChange(3);
		return this;
	}

	private ItemInfo addItem(ItemInstance item)
	{
		ItemInfo info;
		_items.add(info = new ItemInfo(item));
		return info;
	}

	@Override
	protected final void writeImpl()
	{
        writeH(_items.size());
		for(ItemInfo temp : _items)
		{
            writeH(temp.getLastChange());
            writeItemInfo(temp);
		}
	}
}
