package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * Opens the CommandChannel Information window
 */
public class ExOpenMpccPacket implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExOpenMpccPacket();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_OPEN_MPCC.writeId(packetWriter);

		return true;
	}
}