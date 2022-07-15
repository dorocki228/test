package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class TradePressOtherOkPacket implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new TradePressOtherOkPacket();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.TRADE_PRESS_OTHER_OK.writeId(packetWriter);

		return true;
	}
}