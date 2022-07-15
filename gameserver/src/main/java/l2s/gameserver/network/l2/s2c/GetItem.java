package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * 0000: 17  1a 95 20 48  9b da 12 40  44 17 02 00  03 f0 fc ff  98 f1 ff ff                                     .....
 * format  ddddd
 */
public class GetItem implements IClientOutgoingPacket
{
	private int _playerId, _itemObjId;
	private Location _loc;

	public GetItem(ItemInstance item, int playerId)
	{
		_itemObjId = item.getObjectId();
		_loc = item.getLoc();
		_playerId = playerId;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.GET_ITEM.writeId(packetWriter);
		packetWriter.writeD(_playerId);
		packetWriter.writeD(_itemObjId);
		packetWriter.writeD(_loc.x);
		packetWriter.writeD(_loc.y);
		packetWriter.writeD(_loc.z);

		return true;
	}
}