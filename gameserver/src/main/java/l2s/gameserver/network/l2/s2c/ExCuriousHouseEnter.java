package l2s.gameserver.network.l2.s2c;

public class ExCuriousHouseEnter extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC;

	public void ExCuriousHouseEnter()
	{}

	@Override
	protected void writeImpl()
	{}

	static
	{
		STATIC = new ExCuriousHouseEnter();
	}
}
