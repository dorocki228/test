package l2s.gameserver.network.l2.s2c;

public class TradeDonePacket extends L2GameServerPacket
{
	public static final L2GameServerPacket SUCCESS;
	public static final L2GameServerPacket FAIL;
	private final int _response;

	private TradeDonePacket(int num)
	{
		_response = num;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_response);
	}

	static
	{
		SUCCESS = new TradeDonePacket(1);
		FAIL = new TradeDonePacket(0);
	}
}
