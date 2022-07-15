package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * Close the CommandChannel Information window
 */
public class ExCloseMPCCPacket implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExCloseMPCCPacket();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CLOSE_MPCC.writeId(packetWriter);

		return true;
	}
}
