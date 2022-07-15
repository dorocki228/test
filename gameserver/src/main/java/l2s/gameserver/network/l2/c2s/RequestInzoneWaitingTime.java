package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExInzoneWaitingInfo;

/**
 * @author Bonux
**/
public class RequestInzoneWaitingTime implements IClientIncomingPacket
{
	private boolean _openWindow;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_openWindow = packet.readC() > 0;
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		activeChar.sendPacket(new ExInzoneWaitingInfo(activeChar, _openWindow));
	}
}