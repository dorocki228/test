package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExPrivateStoreBuyingResult implements IClientOutgoingPacket
{
	private final int _itemObjId;
	private final long _itemCount;
	private final String _sellerName;

	public ExPrivateStoreBuyingResult(int itemObjId, long itemCount, String sellerName)
	{
		_itemObjId = itemObjId;
		_itemCount = itemCount;
		_sellerName = sellerName;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PRIVATE_STORE_BUYING_RESULT.writeId(packetWriter);
		packetWriter.writeD(_itemObjId);
		packetWriter.writeQ(_itemCount);
		packetWriter.writeS(_sellerName);

		return true;
	}
}