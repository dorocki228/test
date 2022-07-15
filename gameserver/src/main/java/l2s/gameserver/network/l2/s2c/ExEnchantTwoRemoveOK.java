package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public final class ExEnchantTwoRemoveOK implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExEnchantTwoRemoveOK();

	public ExEnchantTwoRemoveOK()
	{
		//
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ENCHANT_TWO_REMOVE_OK.writeId(packetWriter);
		//

		return true;
	}
}