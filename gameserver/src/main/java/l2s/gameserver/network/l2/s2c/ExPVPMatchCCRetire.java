package l2s.gameserver.network.l2.s2c;

public class ExPVPMatchCCRetire extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC;

	@Override
	public void writeImpl()
	{}

	static
	{
		STATIC = new ExPVPMatchCCRetire();
	}
}
