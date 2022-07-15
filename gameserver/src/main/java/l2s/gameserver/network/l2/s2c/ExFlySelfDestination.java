package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExFlySelfDestination implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_FLY_SELF_DESTINATION.writeId(packetWriter);
		// TODO dddd

		return true;
	}
}