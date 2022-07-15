package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExRemoveEnchantSupportItemResult implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExRemoveEnchantSupportItemResult();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_REMOVE_ENCHANT_SUPPORT_ITEM_RESULT.writeId(packetWriter);
		//

		return true;
	}
}