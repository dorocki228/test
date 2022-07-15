package l2s.gameserver.network.l2.c2s;

/**
 * format: chS
 */
public class RequestPCCafeCouponUse implements IClientIncomingPacket
{
	// format: (ch)S
	private String _unknown;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_unknown = packet.readS();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		//TODO not implemented
	}
}