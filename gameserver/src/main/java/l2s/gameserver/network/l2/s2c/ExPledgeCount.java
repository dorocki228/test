package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExPledgeCount implements IClientOutgoingPacket
{
	private final int _count;

	public ExPledgeCount(int count)
	{
		_count = count;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PLEDGE_COUNT.writeId(packetWriter);
		packetWriter.writeD(_count);

		return true;
	}
}