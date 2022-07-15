package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExClosePartyRoomPacket implements IClientOutgoingPacket
{
	public static IClientOutgoingPacket STATIC = new ExClosePartyRoomPacket();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_DISMISPARTY_ROOM.writeId(packetWriter);

		return true;
	}
}