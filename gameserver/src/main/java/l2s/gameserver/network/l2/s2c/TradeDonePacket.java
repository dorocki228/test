package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class TradeDonePacket implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket SUCCESS = new TradeDonePacket(1);
	public static final IClientOutgoingPacket FAIL = new TradeDonePacket(0);

	private int _response;

	private TradeDonePacket(int num)
	{
		_response = num;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.TRADE_DONE.writeId(packetWriter);
		packetWriter.writeD(_response);

		return true;
	}
}