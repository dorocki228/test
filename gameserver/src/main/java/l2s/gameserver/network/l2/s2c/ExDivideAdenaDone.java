package l2s.gameserver.network.l2.s2c;

public class ExDivideAdenaDone extends L2GameServerPacket
{
	private final int _friendsCount;
	private final long _count;
	private final long _dividedCount;
	private final String _name;

	public ExDivideAdenaDone(int friendsCount, long count, long dividedCount, String name)
	{
		_friendsCount = friendsCount;
		_count = count;
		_dividedCount = dividedCount;
		_name = name;
	}

	@Override
	protected final void writeImpl()
	{
        writeC(1);
        writeC(0);
        writeD(_friendsCount);
		writeQ(_dividedCount);
		writeQ(_count);
		writeS(_name);
	}
}
