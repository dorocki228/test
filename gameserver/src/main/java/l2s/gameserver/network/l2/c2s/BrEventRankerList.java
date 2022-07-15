package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.network.l2.s2c.ExBR_LoadEventTopRankers;

public class BrEventRankerList implements IClientIncomingPacket
{
	private static final String _C__D0_7B_BREVENTRANKERLIST = "[C] D0:7B BrEventRankerList";

	private int _eventId;
	private int _day;
	@SuppressWarnings("unused")
	private int _ranking;
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_eventId = packet.readD();
		_day = packet.readD(); // 0 - current, 1 - previous
		_ranking = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		// TODO count, bestScore, myScore
		int count = 0;
		int bestScore = 0;
		int myScore = 0;
		client.sendPacket(new ExBR_LoadEventTopRankers(_eventId, _day, count, bestScore, myScore));
	}

	@Override
	public String getType()
	{
		return _C__D0_7B_BREVENTRANKERLIST;
	}

}