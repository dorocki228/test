package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExFieldEventStep implements IClientOutgoingPacket
{
	private final int _own;
	private final int _cumulative;
	private final int _max;

	public ExFieldEventStep(int own, int cumulative, int max)
	{
		_own = own;
		_cumulative = cumulative;
		_max = max;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_FIELD_EVENT_STEP.writeId(packetWriter);
		packetWriter.writeD(_own);
		packetWriter.writeD(_cumulative);
		packetWriter.writeD(_max);

		return true;
	}
}