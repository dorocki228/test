package l2s.gameserver.network.l2.s2c;

public class ExOlympiadMatchEndPacket extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC;

	@Override
	protected void writeImpl()
	{}

	static
	{
		STATIC = new ExOlympiadMatchEndPacket();
	}
}