package l2s.gameserver.network.l2.s2c;

public class ExBR_NewIConCashBtnWnd extends L2GameServerPacket
{
	public static final L2GameServerPacket HAS_UPDATES;
	public static final L2GameServerPacket NO_UPDATES;
	private final int _value;

	public ExBR_NewIConCashBtnWnd(int value)
	{
		_value = value;
	}

	@Override
	protected void writeImpl()
	{
        writeH(_value);
	}

	static
	{
		HAS_UPDATES = new ExLightingCandleEvent(1);
		NO_UPDATES = new ExLightingCandleEvent(0);
	}
}
