package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.network.l2.s2c.SendStatus;

public final class RequestStatus implements IClientIncomingPacket
{
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		client.close(new SendStatus());
	}
}