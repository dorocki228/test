package l2s.gameserver.network.l2.s2c;

public class ExClosePartyRoomPacket extends L2GameServerPacket
{
	public static L2GameServerPacket STATIC;

	@Override
	protected void writeImpl()
	{}

	static
	{
		STATIC = new ExClosePartyRoomPacket();
	}
}
