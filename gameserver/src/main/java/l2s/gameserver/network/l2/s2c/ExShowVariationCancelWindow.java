package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExShowVariationCancelWindow implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExShowVariationCancelWindow();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_VARIATION_CANCEL_WINDOW.writeId(packetWriter);

		return true;
	}
}