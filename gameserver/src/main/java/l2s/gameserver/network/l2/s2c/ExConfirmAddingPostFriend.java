package l2s.gameserver.network.l2.s2c;

public class ExConfirmAddingPostFriend extends L2GameServerPacket
{
	public static int NAME_IS_NOT_EXISTS;
	public static int SUCCESS;
	public static int PREVIOS_NAME_IS_BEEN_REGISTERED;
	public static int NAME_IS_NOT_EXISTS2;
	public static int LIST_IS_FULL;
	public static int ALREADY_ADDED;
	public static int NAME_IS_NOT_REGISTERED;
	private final String _name;
	private final int _result;

	public ExConfirmAddingPostFriend(String name, int s)
	{
		_name = name;
		_result = s;
	}

	@Override
	public void writeImpl()
	{
		writeS(_name);
        writeD(_result);
	}

	static
	{
		NAME_IS_NOT_EXISTS = 0;
		SUCCESS = 1;
		PREVIOS_NAME_IS_BEEN_REGISTERED = -1;
		NAME_IS_NOT_EXISTS2 = -2;
		LIST_IS_FULL = -3;
		ALREADY_ADDED = -4;
		NAME_IS_NOT_REGISTERED = -4;
	}
}
