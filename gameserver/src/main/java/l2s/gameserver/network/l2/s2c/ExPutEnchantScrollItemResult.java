package l2s.gameserver.network.l2.s2c;

public class ExPutEnchantScrollItemResult extends L2GameServerPacket
{
	public static final L2GameServerPacket FAIL;
	private final int _result;

	public ExPutEnchantScrollItemResult(int result)
	{
		_result = result;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_result);
	}

	static
	{
		FAIL = new ExPutEnchantScrollItemResult(0);
	}
}
