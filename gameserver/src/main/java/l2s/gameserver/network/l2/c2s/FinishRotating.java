package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

/**
 * format:		cdd
 */
public class FinishRotating implements IClientIncomingPacket
{
	private int _degree;
	@SuppressWarnings("unused")
	private int _unknown;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_degree = packet.readD();
		_unknown = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;
		activeChar.broadcastPacket(new l2s.gameserver.network.l2.s2c.FinishRotating(activeChar, _degree, 0));
	}
}