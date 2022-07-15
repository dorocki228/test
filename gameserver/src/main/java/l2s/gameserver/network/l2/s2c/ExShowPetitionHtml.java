package l2s.gameserver.network.l2.s2c;

public class ExShowPetitionHtml implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		// FIXME OutgoingExPackets.PETITION.writeId(packet);
		// TODO dx[dcS]

		return true;
	}
}