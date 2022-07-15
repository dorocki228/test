package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExNotifyPremiumItem implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExNotifyPremiumItem();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_NOTIFY_PREMIUM_ITEM.writeId(packetWriter);

		return true;
	}
}