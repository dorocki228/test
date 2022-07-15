package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

/**
 * @author Sdw
 */
public class RequestStopMove implements IClientIncomingPacket
{
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		activeChar.getMovement().stopMove();
	}
}