package l2s.gameserver.network.l2.s2c;

/**
 * @author monithly
 */
public class ExDeletePartySubstitute implements IClientOutgoingPacket
{
	private final int _obj;

	public ExDeletePartySubstitute(final int objectId)
	{
		_obj = objectId;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		// FIXME OutgoingExPackets..writeId(packet);
		packetWriter.writeD(_obj);

		return true;
	}
}
