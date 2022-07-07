package l2s.gameserver.network.l2.s2c;

public class ExBR_LoadEventTopRankers extends L2GameServerPacket
{
	private final int _eventId;
	private final int _day;
	private final int _count;
	private final int _bestScore;
	private final int _myScore;

	public ExBR_LoadEventTopRankers(int eventId, int day, int count, int bestScore, int myScore)
	{
		_eventId = eventId;
		_day = day;
		_count = count;
		_bestScore = bestScore;
		_myScore = myScore;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_eventId);
        writeD(_day);
        writeD(_count);
        writeD(_bestScore);
        writeD(_myScore);
	}
}
