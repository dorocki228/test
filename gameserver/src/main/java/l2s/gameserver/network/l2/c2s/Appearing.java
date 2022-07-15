package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class Appearing implements IClientIncomingPacket
{
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		final Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isLogoutStarted())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getObserverMode() == Player.OBSERVER_STARTING)
		{
			activeChar.appearObserverMode();
			return;
		}

		if(activeChar.getObserverMode() == Player.OBSERVER_LEAVING)
		{
			activeChar.returnFromObserverMode();
			return;
		}

		if(!activeChar.isTeleporting())
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.onTeleported();
	}
}