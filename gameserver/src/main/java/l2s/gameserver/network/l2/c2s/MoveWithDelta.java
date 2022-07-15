package l2s.gameserver.network.l2.c2s;

/**
 * Format: (c) ddd
 * d: dx
 * d: dy
 * d: dz
 */
public class MoveWithDelta implements IClientIncomingPacket
{
	@SuppressWarnings("unused")
	private int _dx, _dy, _dz;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_dx = packet.readD();
		_dy = packet.readD();
		_dz = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		// TODO this
	}
}