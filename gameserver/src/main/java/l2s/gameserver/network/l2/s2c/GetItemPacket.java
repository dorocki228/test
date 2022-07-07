package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.utils.Location;

public class GetItemPacket extends L2GameServerPacket
{
	private final int _playerId;
	private final int _itemObjId;
	private final Location _loc;

	public GetItemPacket(ItemInstance item, int playerId)
	{
		_itemObjId = item.getObjectId();
		_loc = item.getLoc();
		_playerId = playerId;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_playerId);
        writeD(_itemObjId);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z);
	}
}
