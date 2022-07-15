package l2s.gameserver.network.l2.s2c;

import java.util.Map;

import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.network.l2.OutgoingPackets;

public class ShopPreviewInfoPacket implements IClientOutgoingPacket
{
	private Map<Integer, Integer> _itemlist;

	public ShopPreviewInfoPacket(Map<Integer, Integer> itemlist)
	{
		_itemlist = itemlist;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.SHOP_PREVIEW_INFO.writeId(packetWriter);
		packetWriter.writeD(Inventory.PAPERDOLL_MAX);

		// Slots
		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
			packetWriter.writeD(getFromList(PAPERDOLL_ID));

		return true;
	}

	private int getFromList(int key)
	{
		return ((_itemlist.get(key) != null) ? _itemlist.get(key) : 0);
	}
}