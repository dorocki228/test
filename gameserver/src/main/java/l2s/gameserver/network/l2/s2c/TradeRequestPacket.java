package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class TradeRequestPacket implements IClientOutgoingPacket
{
	private int _senderId;

	public TradeRequestPacket(int senderId)
	{
		_senderId = senderId;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.TRADE_REQUEST.writeId(packetWriter);
		packetWriter.writeD(_senderId);

		return true;
	}
}