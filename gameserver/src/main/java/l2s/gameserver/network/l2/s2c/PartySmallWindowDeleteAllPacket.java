package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class PartySmallWindowDeleteAllPacket implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new PartySmallWindowDeleteAllPacket();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PARTY_SMALL_WINDOW_DELETE_ALL.writeId(packetWriter);

		return true;
	}
}