package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class LogOutOkPacket implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new LogOutOkPacket();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.LOG_OUT_OK.writeId(packetWriter);

		return true;
	}
}