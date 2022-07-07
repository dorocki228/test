package l2s.gameserver.network.l2.s2c;

public class ExPut_Shape_Shifting_Target_Item_Result extends L2GameServerPacket
{
	public static L2GameServerPacket FAIL;
	public static int SUCCESS_RESULT;
	private final int _resultId;
	private final long _price;

	public ExPut_Shape_Shifting_Target_Item_Result(int resultId, long price)
	{
		_resultId = resultId;
		_price = price;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_resultId);
		writeQ(_price);
	}

	static
	{
		FAIL = new ExPut_Shape_Shifting_Target_Item_Result(0, 0L);
		SUCCESS_RESULT = 1;
	}
}
