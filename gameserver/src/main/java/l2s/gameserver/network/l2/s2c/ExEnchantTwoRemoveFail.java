package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public final class ExEnchantTwoRemoveFail implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExEnchantTwoRemoveFail();

	public ExEnchantTwoRemoveFail()
	{
		//
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ENCHANT_TWO_REMOVE_FAIL.writeId(packetWriter);
		//

		return true;
	}
}