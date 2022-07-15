package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExEventMatchMessage implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_EVENT_MATCH_MESSAGE.writeId(packetWriter);
		// TODO cS

		return true;
	}
}