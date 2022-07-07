package l2s.gameserver.network.l2.s2c;

public class ExDominionChannelSet extends L2GameServerPacket
{
	public static final L2GameServerPacket ACTIVE;
	public static final L2GameServerPacket DEACTIVE;
	private final int _active;

	public ExDominionChannelSet(int active)
	{
		_active = active;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_active);
	}

	static
	{
		ACTIVE = new ExDominionChannelSet(1);
		DEACTIVE = new ExDominionChannelSet(0);
	}
}
