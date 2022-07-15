package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExEventMatchObserver implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_EVENT_MATCH_OBSERVER.writeId(packetWriter);
		// TODO dccSS

		return true;
	}
}