package l2s.gameserver.network.l2.s2c;

public class DicePacket extends L2GameServerPacket
{
	private final int _playerId;
	private final int _itemId;
	private final int _number;
	private final int _x;
	private final int _y;
	private final int _z;

	public DicePacket(int playerId, int itemId, int number, int x, int y, int z)
	{
		_playerId = playerId;
		_itemId = itemId;
		_number = number;
		_x = x;
		_y = y;
		_z = z;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_playerId);
        writeD(_itemId);
        writeD(_number);
        writeD(_x);
        writeD(_y);
        writeD(_z);
	}
}
