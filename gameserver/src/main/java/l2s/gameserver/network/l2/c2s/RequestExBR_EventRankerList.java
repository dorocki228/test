package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.network.l2.s2c.ExBR_LoadEventTopRankers;

public class RequestExBR_EventRankerList extends L2GameClientPacket
{
	private static final String _C__D0_7B_BREVENTRANKERLIST = "[C] D0:7B BrEventRankerList";
	private int _eventId;
	private int _day;
	private int _ranking;

	@Override
	protected void readImpl()
	{
		_eventId = readD();
		_day = readD();
		_ranking = readD();
	}

	@Override
	protected void runImpl()
	{
		int count = 0;
		int bestScore = 0;
		int myScore = 0;
		getClient().sendPacket(new ExBR_LoadEventTopRankers(_eventId, _day, count, bestScore, myScore));
	}

	@Override
	public String getType()
	{
		return "[C] D0:7B BrEventRankerList";
	}
}
