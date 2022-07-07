package l2s.gameserver.network.l2.s2c;

public class ExSetPartyLooting extends L2GameServerPacket
{
	private final int _result;
	private final int _mode;

	public ExSetPartyLooting(int result, int mode)
	{
		_result = result;
		_mode = mode;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_result);
        writeD(_mode);
	}
}
