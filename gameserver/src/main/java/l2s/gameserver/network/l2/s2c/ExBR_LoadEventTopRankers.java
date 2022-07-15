package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExBR_LoadEventTopRankers implements IClientOutgoingPacket
{
	private int _eventId;
	private int _day;
	private int _count;
	private int _bestScore;
	private int _myScore;

	public ExBR_LoadEventTopRankers(int eventId, int day, int count, int bestScore, int myScore)
	{
		_eventId = eventId;
		_day = day;
		_count = count;
		_bestScore = bestScore;
		_myScore = myScore;
	}
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_BR_LOAD_EVENT_TOP_RANKERACK.writeId(packetWriter);
		packetWriter.writeD(_eventId);
		packetWriter.writeD(_day);
		packetWriter.writeD(_count);
		packetWriter.writeD(_bestScore);
		packetWriter.writeD(_myScore);

		return true;
	}
}