package l2s.gameserver.network.l2.s2c;

public class ExRegenMaxPacket extends L2GameServerPacket
{
	private final double _max;
	private final int _count;
	private final int _time;
	public static final int POTION_HEALING_GREATER = 16457;
	public static final int POTION_HEALING_MEDIUM = 16440;
	public static final int POTION_HEALING_LESSER = 16416;

	public ExRegenMaxPacket(double max, int count, int time)
	{
		_max = max * 0.66;
		_count = count;
		_time = time;
	}

	@Override
	protected void writeImpl()
	{
        writeD(1);
        writeD(_count);
        writeD(_time);
		writeF(_max);
	}
}
