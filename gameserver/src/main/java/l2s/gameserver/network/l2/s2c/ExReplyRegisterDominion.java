package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author VISTALL
 */
public class ExReplyRegisterDominion implements IClientOutgoingPacket
{
	public ExReplyRegisterDominion()
	{
		//
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_REPLY_REGISTER_DOMINION.writeId(packetWriter);
		packetWriter.writeD(0x00);
		packetWriter.writeD(0x00);
		packetWriter.writeD(0x00);
		packetWriter.writeD(0x00);
		packetWriter.writeD(0x00);
		packetWriter.writeD(0x00);

		return true;
	}
}