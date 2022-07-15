package l2s.gameserver.network.l2.c2s;

public class RequestCreatePledge implements IClientIncomingPacket
{
	//Format: cS
	private String _pledgename;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_pledgename = packet.readS(64);
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		//TODO not implemented
	}
}