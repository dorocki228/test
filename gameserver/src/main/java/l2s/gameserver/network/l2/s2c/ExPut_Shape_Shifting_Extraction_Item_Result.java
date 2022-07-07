package l2s.gameserver.network.l2.s2c;

public class ExPut_Shape_Shifting_Extraction_Item_Result extends L2GameServerPacket
{
	public static L2GameServerPacket FAIL;
	public static L2GameServerPacket SUCCESS;
	private final int _result;

	public ExPut_Shape_Shifting_Extraction_Item_Result(int result)
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
		FAIL = new ExPut_Shape_Shifting_Extraction_Item_Result(0);
		SUCCESS = new ExPut_Shape_Shifting_Extraction_Item_Result(1);
	}
}
