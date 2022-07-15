package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * Открывает окно аугмента, название от фонаря.
 */
public class ExShowVariationMakeWindow implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExShowVariationMakeWindow();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_VARIATION_MAKE_WINDOW.writeId(packetWriter);

		return true;
	}
}