package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.OutgoingPackets;

public class InventoryUpdatePacket extends AbstractItemPacket
{
	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int MODIFIED = 2;
	public static final int REMOVED = 3;

	private final List<ItemInfo> _items = new ArrayList<ItemInfo>(1);

	public InventoryUpdatePacket addNewItem(Player player, ItemInstance item)
	{
		addItem(player, item).setLastChange(ADDED);
		return this;
	}

	public InventoryUpdatePacket addModifiedItem(Player player, ItemInstance item)
	{
		addItem(player, item).setLastChange(MODIFIED);
		return this;
	}

	public InventoryUpdatePacket addRemovedItem(Player player, ItemInstance item)
	{
		addItem(player, item).setLastChange(REMOVED);
		return this;
	}

	private ItemInfo addItem(Player player, ItemInstance item)
	{
		ItemInfo info;
		_items.add(info = new ItemInfo(player, item, item.getTemplate().isBlocked(player, item)));
		return info;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.INVENTORY_UPDATE.writeId(packetWriter);
		packetWriter.writeC(0x00);	// 140 PROTOCOL
		packetWriter.writeD(_items.size());
		packetWriter.writeD(_items.size());
		for(ItemInfo temp : _items)
		{
			packetWriter.writeH(temp.getLastChange());
			writeItem(packetWriter, temp);
		}

		return true;
	}
}