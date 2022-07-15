package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExSetPledgeEmblemAck implements IClientOutgoingPacket
{
	private final int _part;

	public ExSetPledgeEmblemAck(int part)
	{
		_part = part;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SET_PLEDGE_EMBLEM_ACK.writeId(packetWriter);
		packetWriter.writeD(_part);

		return true;
	}
}