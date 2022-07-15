package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExItemAuctionStatus implements IClientOutgoingPacket
{
	public ExItemAuctionStatus()
	{
		//
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ITEM_AUCTION_STATUS.writeId(packetWriter);
		packetWriter.writeH(0x00);
		packetWriter.writeH(0x00);
		packetWriter.writeH(0x00);
		packetWriter.writeH(0x00);
		packetWriter.writeH(0x00);
		packetWriter.writeH(0x00);
		packetWriter.writeD(0x00);
		packetWriter.writeC(0x00);

		return true;
	}
}