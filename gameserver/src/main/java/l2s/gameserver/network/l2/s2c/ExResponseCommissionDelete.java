package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExResponseCommissionDelete implements IClientOutgoingPacket
{
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_RESPONSE_COMMISSION_DELETE.writeId(packetWriter);
		packetWriter.writeD(0x00);
		packetWriter.writeD(0x00);
		packetWriter.writeQ(0x00);

		return true;
	}
}
