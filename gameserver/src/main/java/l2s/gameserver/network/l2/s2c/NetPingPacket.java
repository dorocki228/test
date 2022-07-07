package l2s.gameserver.network.l2.s2c;

public class NetPingPacket extends L2GameServerPacket
{
	private final int timestamp;

	public NetPingPacket(int timestamp)
	{
		this.timestamp = timestamp;
	}

	@Override
	protected void writeImpl()
	{
        writeD(timestamp);
	}
}
