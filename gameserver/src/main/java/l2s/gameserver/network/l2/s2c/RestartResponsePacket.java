package l2s.gameserver.network.l2.s2c;

public class RestartResponsePacket extends L2GameServerPacket
{
	public static final RestartResponsePacket OK;
	public static final RestartResponsePacket FAIL;
	private final String _message;
	private final int _param;

	public RestartResponsePacket(int param)
	{
		_message = "bye";
		_param = param;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_param);
		writeS(_message);
	}

	static
	{
		OK = new RestartResponsePacket(1);
		FAIL = new RestartResponsePacket(0);
	}
}
