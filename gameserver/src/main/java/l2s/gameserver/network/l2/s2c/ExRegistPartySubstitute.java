package l2s.gameserver.network.l2.s2c;

public class ExRegistPartySubstitute implements IClientOutgoingPacket
{
	private final int _object;

	public ExRegistPartySubstitute(int obj)
	{
		_object = obj;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		// FIXME OutgoingExPackets..writeId(packet);
		packetWriter.writeD(_object);
		packetWriter.writeD(0x01);

		return true;
	}
}
