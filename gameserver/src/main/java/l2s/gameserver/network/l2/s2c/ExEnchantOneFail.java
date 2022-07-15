package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public final class ExEnchantOneFail implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExEnchantOneFail();

	public ExEnchantOneFail()
	{
		//
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ENCHANT_ONE_FAIL.writeId(packetWriter);
		//

		return true;
	}
}