package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * 15
 * ee cc 11 43 		object id
 * 39 00 00 00 		item id
 * 8f 14 00 00 		x
 * b7 f1 00 00 		y
 * 60 f2 ff ff 		z
 * 01 00 00 00 		show item count
 * 7a 00 00 00      count                                         .
 *
 * format  dddddddd
 */
public class SpawnItemPacket implements IClientOutgoingPacket
{
	private int _objectId;
	private int _itemId;
	private int _x, _y, _z;
	private int _stackable;
	private long _count;
	private final int _enchantLevel;
	private final boolean _augmented;
	private final int _ensoulCount;

	public SpawnItemPacket(ItemInstance item)
	{
		_objectId = item.getObjectId();
		_itemId = item.getItemId();
		_x = item.getX();
		_y = item.getY();
		_z = item.getZ();
		_stackable = item.isStackable() ? 0x01 : 0x00;
		_count = item.getCount();
		_enchantLevel = item.getEnchantLevel();
		_augmented = item.isAugmented();
		_ensoulCount = item.getNormalEnsouls().length + item.getSpecialEnsouls().length;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.SPAWN_ITEM.writeId(packetWriter);
		packetWriter.writeD(_objectId);
		packetWriter.writeD(_itemId);

		packetWriter.writeD(_x);
		packetWriter.writeD(_y);
		packetWriter.writeD(_z);
		packetWriter.writeD(_stackable);
		packetWriter.writeQ(_count);
		packetWriter.writeD(0x00); //c2
		packetWriter.writeC(_enchantLevel);
		packetWriter.writeC(_augmented);
		packetWriter.writeC(_ensoulCount);

		return true;
	}
}