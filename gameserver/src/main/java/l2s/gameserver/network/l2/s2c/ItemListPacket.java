package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.network.l2.OutgoingPackets;

import java.util.Collection;

public class ItemListPacket extends AbstractItemPacket
{
	private final boolean _showWindow;
	private final int _type;
	private final Player player;
	private final int size;
	private final Collection<ItemInfo> items;

	public ItemListPacket(int type, Player player, int size, Collection<ItemInfo> items, boolean showWindow)
	{
		_type = type;
		this.player = player;
		this.size = size;
		_showWindow = showWindow;
		this.items = items;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.ITEM_LIST.writeId(packetWriter);
		if (_type == 2)
		{
			packetWriter.writeC(_type);
			packetWriter.writeD(size);	// Total items
			packetWriter.writeD(size);	// Items in this page
			for (ItemInfo temp : items)
			{
				writeItem(packetWriter, temp);
			}
		}
		else
		{
			packetWriter.writeC(_type);
			packetWriter.writeH(_showWindow);
			writeInventoryBlock(packetWriter, player.getInventory());
			packetWriter.writeD(size);	// Total items
		}

		return true;
	}
}