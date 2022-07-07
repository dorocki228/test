package l2s.gameserver.network.l2.s2c;

public class ExPutEnchantTargetItemResult extends L2GameServerPacket
{
	public static final L2GameServerPacket FAIL;
	public static final L2GameServerPacket SUCCESS;
	private final int _result;

	public ExPutEnchantTargetItemResult(int result)
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
		FAIL = new ExPutEnchantTargetItemResult(0);
		SUCCESS = new ExPutEnchantTargetItemResult(1);
	}
}
