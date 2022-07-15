package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

/**
 * 
 * @author n0nam3
 * @date 22/08/2010 15:00
 *
 */
public class RequestResetNickname implements IClientIncomingPacket
{
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		// nothing (trigger)
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		final Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.getTitleColor() != Player.DEFAULT_TITLE_COLOR)
		{
			activeChar.setTitleColor(Player.DEFAULT_TITLE_COLOR);
			activeChar.broadcastUserInfo(true);
		}
	}
}