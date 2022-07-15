package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
 */
public class ExChangeAttributeItemList extends AbstractItemPacket
{
	private ItemInfo[] _itemsList;
	private int _itemId;

	public ExChangeAttributeItemList(int itemId, ItemInfo[] itemsList)
	{
		_itemId = itemId;
		_itemsList = itemsList;
	}

	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CHANGE_ATTRIBUTE_ITEM_LIST.writeId(packetWriter);
		packetWriter.writeD(_itemId);
		packetWriter.writeD(_itemsList.length); //size
		for(ItemInfo item : _itemsList)
		{
			writeItem(packetWriter, item);
		}

		return true;
	}
}