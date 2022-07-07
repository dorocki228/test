package l2s.gameserver.network.l2.s2c;

public class ExIsCharNameCreatable extends L2GameServerPacket
{
	public static final L2GameServerPacket SUCCESS;
	public static final L2GameServerPacket UNABLE_TO_CREATE_A_CHARACTER;
	public static final L2GameServerPacket TOO_MANY_CHARACTERS;
	public static final L2GameServerPacket NAME_ALREADY_EXISTS;
	public static final L2GameServerPacket ENTER_CHAR_NAME__MAX_16_CHARS;
	public static final L2GameServerPacket WRONG_NAME;
	public static final L2GameServerPacket WRONG_SERVER;
	public static final L2GameServerPacket DONT_CREATE_CHARS_ON_THIS_SERVER;
	public static final L2GameServerPacket DONT_USE_ENG_CHARS;
	public int _errorCode;

	public ExIsCharNameCreatable(int errorCode)
	{
		_errorCode = errorCode;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_errorCode);
	}

	static
	{
		SUCCESS = new ExIsCharNameCreatable(-1);
		UNABLE_TO_CREATE_A_CHARACTER = new ExIsCharNameCreatable(0);
		TOO_MANY_CHARACTERS = new ExIsCharNameCreatable(1);
		NAME_ALREADY_EXISTS = new ExIsCharNameCreatable(2);
		ENTER_CHAR_NAME__MAX_16_CHARS = new ExIsCharNameCreatable(3);
		WRONG_NAME = new ExIsCharNameCreatable(4);
		WRONG_SERVER = new ExIsCharNameCreatable(5);
		DONT_CREATE_CHARS_ON_THIS_SERVER = new ExIsCharNameCreatable(6);
		DONT_USE_ENG_CHARS = new ExIsCharNameCreatable(7);
	}
}
