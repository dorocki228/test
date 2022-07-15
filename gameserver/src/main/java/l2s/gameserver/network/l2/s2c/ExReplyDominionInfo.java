package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExReplyDominionInfo implements IClientOutgoingPacket
{
	public ExReplyDominionInfo()
	{
		//
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_REPLY_DOMINION_INFO.writeId(packetWriter);
		packetWriter.writeD(0x00);

		return true;
	}
}