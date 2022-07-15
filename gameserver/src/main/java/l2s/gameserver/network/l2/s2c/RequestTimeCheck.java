package l2s.gameserver.network.l2.s2c;

public class RequestTimeCheck implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		// FIXME OutgoingPackets..writeId(packet);
		//TODO d

		return true;
	}
}