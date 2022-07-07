package l2s.gameserver.network.l2.s2c;

public class Ex2NDPasswordCheckPacket extends L2GameServerPacket
{
	public static final int PASSWORD_NEW = 0;
	public static final int PASSWORD_PROMPT = 1;
	public static final int PASSWORD_OK = 2;
	private final int _windowType;

	public Ex2NDPasswordCheckPacket(int windowType)
	{
		_windowType = windowType;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_windowType);
        writeD(0);
	}
}
