package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.network.l2.OutgoingExPackets;

import java.util.Collection;

/**
 * @author VISTALL
 * @date 1:02/23.02.2011
 */
public class ExQuestItemListPacket extends AbstractItemPacket
{
	private final Player player;
	private final int _type;
	private final int size;
	private final Collection<ItemInfo> items;

	public ExQuestItemListPacket(int type, Player player, int size, Collection<ItemInfo> items)
	{
		_type = type;
		this.player = player;
		this.size = size;
		this.items = items;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_QUEST_ITEMLIST.writeId(packetWriter);
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
			writeInventoryBlock(packetWriter, player.getInventory());
			packetWriter.writeD(size);	// Total items
		}

		return true;
	}
}
