package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExMailArrived implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExMailArrived();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_MAIL_ARRIVED.writeId(packetWriter);

		return true;
	}
}