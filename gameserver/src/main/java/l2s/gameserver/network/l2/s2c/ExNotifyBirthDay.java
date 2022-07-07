package l2s.gameserver.network.l2.s2c;

public class ExNotifyBirthDay extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC;

	@Override
	protected void writeImpl()
	{
        writeD(0);
	}

	static
	{
		STATIC = new ExNotifyBirthDay();
	}
}
