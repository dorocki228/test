package l2s.gameserver.network.l2.s2c;

public class ExCuriousHouseState extends L2GameServerPacket
{
	public static final L2GameServerPacket IDLE;
	public static final L2GameServerPacket INVITE;
	public static final L2GameServerPacket PREPARE;
	private final int _state;

	public ExCuriousHouseState(int state)
	{
		_state = state;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_state);
	}

	static
	{
		IDLE = new ExCuriousHouseState(0);
		INVITE = new ExCuriousHouseState(1);
		PREPARE = new ExCuriousHouseState(2);
	}
}
