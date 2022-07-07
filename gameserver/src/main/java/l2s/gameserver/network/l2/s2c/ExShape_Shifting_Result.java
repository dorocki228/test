package l2s.gameserver.network.l2.s2c;

public class ExShape_Shifting_Result extends L2GameServerPacket
{
	public static L2GameServerPacket FAIL;
	public static int SUCCESS_RESULT;
	private final int _result;
	private final int _targetItemId;
	private final int _extractItemId;
	private final int _period;

	public ExShape_Shifting_Result(int result, int targetItemId, int extractItemId, int period)
	{
		_result = result;
		_targetItemId = targetItemId;
		_extractItemId = extractItemId;
		_period = period;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_result);
        writeD(_targetItemId);
        writeD(_extractItemId);
        writeD(_period);
	}

	static
	{
		FAIL = new ExShape_Shifting_Result(0, 0, 0, -1);
		SUCCESS_RESULT = 1;
	}
}
