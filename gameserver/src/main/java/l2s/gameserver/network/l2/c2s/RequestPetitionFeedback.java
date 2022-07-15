package l2s.gameserver.network.l2.c2s;

/**
 * format: ddS
 */
public class RequestPetitionFeedback implements IClientIncomingPacket
{
	private int _type, _unk1;
	private String _petitionText;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_type = packet.readD();
		_unk1 = packet.readD(); // possible always zero
		_petitionText = packet.readS(4096);
		// not done
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		//
	}
}