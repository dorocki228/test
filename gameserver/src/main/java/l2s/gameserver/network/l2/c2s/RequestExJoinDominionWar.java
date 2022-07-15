package l2s.gameserver.network.l2.c2s;

public class RequestExJoinDominionWar implements IClientIncomingPacket
{
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		packet.readD();
		packet.readD();
		packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		//
	}
}