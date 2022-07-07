package l2s.gameserver.network.l2.s2c;

public class ExWaitWaitingSubStituteInfo extends L2GameServerPacket
{
	public static final L2GameServerPacket OPEN;
	public static final L2GameServerPacket CLOSE;
	private final boolean _open;

	public ExWaitWaitingSubStituteInfo(boolean open)
	{
		_open = open;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_open);
	}

	static
	{
		OPEN = new ExWaitWaitingSubStituteInfo(true);
		CLOSE = new ExWaitWaitingSubStituteInfo(false);
	}
}
