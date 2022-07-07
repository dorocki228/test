package l2s.gameserver.network.l2.s2c;

public class Ex2NDPasswordVerifyPacket extends L2GameServerPacket
{
	public static final int PASSWORD_OK = 0;
	public static final int PASSWORD_WRONG = 1;
	public static final int PASSWORD_BAN = 2;
	private final int _wrongTentatives;
	private final int _mode;

	public Ex2NDPasswordVerifyPacket(int mode, int wrongTentatives)
	{
		_mode = mode;
		_wrongTentatives = wrongTentatives;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_mode);
        writeD(_wrongTentatives);
	}
}
