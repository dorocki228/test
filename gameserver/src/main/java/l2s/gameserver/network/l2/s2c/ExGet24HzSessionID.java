package l2s.gameserver.network.l2.s2c;

public class ExGet24HzSessionID implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		// FIXME OutgoingExPackets..writeId(packet);
		//TODO: [Bonux]

		return true;
	}
}