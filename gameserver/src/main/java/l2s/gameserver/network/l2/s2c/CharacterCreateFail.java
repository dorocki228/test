package l2s.gameserver.network.l2.s2c;

public class CharacterCreateFail extends L2GameServerPacket
{
	public static final L2GameServerPacket REASON_CREATION_FAILED = new CharacterCreateFail(0x00);
	public static final L2GameServerPacket REASON_TOO_MANY_CHARACTERS = new CharacterCreateFail(0x01);
	public static final L2GameServerPacket REASON_NAME_ALREADY_EXISTS = new CharacterCreateFail(0x02);
	public static final L2GameServerPacket REASON_16_ENG_CHARS = new CharacterCreateFail(0x03);
	public static final L2GameServerPacket REASON_INCORRECT_NAME = new CharacterCreateFail(0x04);
	public static final L2GameServerPacket REASON_CREATE_NOT_ALLOWED = new CharacterCreateFail(0x05);
	public static final L2GameServerPacket REASON_CHOOSE_ANOTHER_SVR = new CharacterCreateFail(0x06);

	private final int _error;

	private CharacterCreateFail(int errorCode)
	{
		_error = errorCode;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_error);
	}
}
