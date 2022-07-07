package l2s.gameserver.network.l2.s2c;

public class ExLightingCandleEvent extends L2GameServerPacket
{
	public static final L2GameServerPacket ENABLED;
	public static final L2GameServerPacket DISABLED;
	private final int _value;

	public ExLightingCandleEvent(int value)
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
		ENABLED = new ExLightingCandleEvent(1);
		DISABLED = new ExLightingCandleEvent(0);
	}
}
