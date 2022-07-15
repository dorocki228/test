package l2s.gameserver.network.l2.c2s;

public class NotifyStartMiniGame implements IClientIncomingPacket
{
	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		// just trigger
	}

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		return true;
	}
}