package l2s.gameserver.network.l2.s2c;

/**
 *
 * @author monithly
 */
public class ExTimeOverPartySubstitute implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		// FIXME OutgoingExPackets..writeId(packet);

		return true;
	}
}
