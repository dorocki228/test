package l2s.gameserver.network.l2.s2c;

public class CharacterDeleteFailPacket extends L2GameServerPacket
{
	public static int REASON_DELETION_FAILED;
	public static int REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER;
	public static int REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED;
	int _error;

	public CharacterDeleteFailPacket(int error)
	{
		_error = error;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_error);
	}

	static
	{
		REASON_DELETION_FAILED = 1;
		REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER = 2;
		REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED = 3;
	}
}
