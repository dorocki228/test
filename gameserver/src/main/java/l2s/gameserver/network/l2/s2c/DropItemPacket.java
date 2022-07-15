package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.OutgoingPackets;

public class DropItemPacket implements IClientOutgoingPacket
{
	private final Location _loc;
	private final int _playerId, item_obj_id, item_id, _stackable;
	private final long _count;
	private final int _enchantLevel;
	private final boolean _augmented;
	private final int _ensoulCount;

	/**
	 * Constructor<?> of the DropItem server packet
	 * @param item : L2ItemInstance designating the item
	 * @param playerId : int designating the player ID who dropped the item
	 */
	public DropItemPacket(ItemInstance item, int playerId)
	{
		_playerId = playerId;
		item_obj_id = item.getObjectId();
		item_id = item.getItemId();
		_loc = item.getLoc();
		_stackable = item.isStackable() ? 1 : 0;
		_count = item.getCount();
		_enchantLevel = item.getEnchantLevel();
		_augmented = item.isAugmented();
		_ensoulCount = item.getNormalEnsouls().length + item.getSpecialEnsouls().length;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.DROP_ITEM.writeId(packetWriter);
		packetWriter.writeD(_playerId);
		packetWriter.writeD(item_obj_id);
		packetWriter.writeD(item_id);
		packetWriter.writeD(_loc.x);
		packetWriter.writeD(_loc.y);
		packetWriter.writeD(_loc.z);
		packetWriter.writeC(_stackable);
		packetWriter.writeQ(_count);
		packetWriter.writeC(1); // unknown
		packetWriter.writeC(_enchantLevel);
		packetWriter.writeC(_augmented);
		packetWriter.writeC(_ensoulCount);
		return true;
	}
}