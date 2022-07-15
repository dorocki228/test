package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExPrivateStoreSellingResult implements IClientOutgoingPacket
{
	private final int _itemObjId;
	private final long _itemCount;
	private final String _buyerName;

	public ExPrivateStoreSellingResult(int itemObjId, long itemCount, String buyerName)
	{
		_itemObjId = itemObjId;
		_itemCount = itemCount;
		_buyerName = buyerName;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PRIVATE_STORE_SELLING_RESULT.writeId(packetWriter);
		packetWriter.writeD(_itemObjId);
		packetWriter.writeQ(_itemCount);
		packetWriter.writeS(_buyerName);

		return true;
	}
}