package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExFieldEventPoint implements IClientOutgoingPacket
{
	private final int _points;

	public ExFieldEventPoint(int points)
	{
		_points = points;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_FIELD_EVENT_POINT.writeId(packetWriter);
		packetWriter.writeD(_points);

		return true;
	}
}