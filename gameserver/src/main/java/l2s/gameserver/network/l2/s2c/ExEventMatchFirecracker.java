package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExEventMatchFirecracker implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_EVENT_MATCH_FIRECRACKER.writeId(packetWriter);
		// TODO d

		return true;
	}
}