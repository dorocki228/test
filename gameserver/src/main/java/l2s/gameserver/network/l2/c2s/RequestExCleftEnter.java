package l2s.gameserver.network.l2.c2s;

public class RequestExCleftEnter implements IClientIncomingPacket
{
	private int unk;

	/**
	 * format: d
	 */
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		unk = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		//TODO not implemented
	}
}