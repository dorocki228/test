package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

//@Deprecated
public class TradePressOwnOkPacket implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.TRADE_PRESS_OWN_OK.writeId(packetWriter);

		return true;
	}
}