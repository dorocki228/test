package l2s.gameserver.network.l2.s2c;

public class ExPutEnchantSupportItemResult extends L2GameServerPacket
{
	public static final L2GameServerPacket FAIL;
	public static final L2GameServerPacket SUCCESS;
	private final int _result;

	public ExPutEnchantSupportItemResult(int result)
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
		FAIL = new ExPutEnchantSupportItemResult(1);
		SUCCESS = new ExPutEnchantSupportItemResult(1);
	}
}
