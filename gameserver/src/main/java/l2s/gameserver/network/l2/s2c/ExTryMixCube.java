package l2s.gameserver.network.l2.s2c;

public class ExTryMixCube extends L2GameServerPacket
{
	public static final L2GameServerPacket FAIL;
	private final int _result;
	private final int _itemId;
	private final long _itemCount;

	public ExTryMixCube(int result)
	{
		_result = result;
		_itemId = 0;
		_itemCount = 0L;
	}

	public ExTryMixCube(int itemId, long itemCount)
	{
		_result = 0;
		_itemId = itemId;
		_itemCount = itemCount;
	}

	@Override
	protected void writeImpl()
	{
        writeC(_result);
        writeD(1);
        writeC(0);
        writeD(_itemId);
		writeQ(_itemCount);
	}

	static
	{
		FAIL = new ExTryMixCube(6);
	}
}
