package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.OutgoingPackets;

public class PetInventoryUpdatePacket extends AbstractItemPacket
{
	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int MODIFIED = 2;
	public static final int REMOVED = 3;

	private final List<ItemInfo> _items = new ArrayList<ItemInfo>(1);

	public PetInventoryUpdatePacket()
	{}

	public PetInventoryUpdatePacket addNewItem(ItemInstance item)
	{
		addItem(item).setLastChange(ADDED);
		return this;
	}

	public PetInventoryUpdatePacket addModifiedItem(ItemInstance item)
	{
		addItem(item).setLastChange(MODIFIED);
		return this;
	}

	public PetInventoryUpdatePacket addRemovedItem(ItemInstance item)
	{
		addItem(item).setLastChange(REMOVED);
		return this;
	}

	private ItemInfo addItem(ItemInstance item)
	{
		ItemInfo info;
		_items.add(info = new ItemInfo(null, item));
		return info;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PET_INVENTORY_UPDATE.writeId(packetWriter);
		packetWriter.writeH(_items.size());
		for(ItemInfo temp : _items)
		{
			packetWriter.writeH(temp.getLastChange());
			writeItem(packetWriter, temp);
		}

		return true;
	}
}