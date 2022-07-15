package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExShowEnsoulExtractionWindow implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExShowEnsoulExtractionWindow();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_ENSOUL_EXTRACTION_WINDOW.writeId(packetWriter);
		// STATIC

		return true;
	}
}