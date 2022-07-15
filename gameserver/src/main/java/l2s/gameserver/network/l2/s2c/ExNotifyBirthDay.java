package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExNotifyBirthDay implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExNotifyBirthDay();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_NOTIFY_BIRTHDAY.writeId(packetWriter);
		packetWriter.writeD(0); // Actor OID

		return true;
	}
}