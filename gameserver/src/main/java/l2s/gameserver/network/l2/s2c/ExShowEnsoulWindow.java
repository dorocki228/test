package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExShowEnsoulWindow implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExShowEnsoulWindow();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_ENSOUL_WINDOW.writeId(packetWriter);
		// STATIC

		return true;
	}
}