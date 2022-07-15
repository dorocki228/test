package l2s.gameserver.network.l2.c2s;

/**
 * Format ch
 * c: (id) 0x39
 * h: (subid) 0x02
 */
class SuperCmdServerStatus implements IClientIncomingPacket
{
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		//
	}
}