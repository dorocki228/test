package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.Ex2NDPasswordCheckPacket;
import l2s.gameserver.security.SecondaryPasswordAuth;

/**
 * Format: (ch)
 */
public class RequestEx2ndPasswordCheck implements IClientIncomingPacket
{
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		SecondaryPasswordAuth spa = client.getSecondaryAuth();
		if(Config.EX_SECOND_AUTH_ENABLED && spa == null)
		{
			client.sendPacket(ActionFailPacket.STATIC);
			return;
		}
		if(!Config.EX_SECOND_AUTH_ENABLED || spa.isAuthed())
		{
			client.sendPacket(new Ex2NDPasswordCheckPacket(Ex2NDPasswordCheckPacket.PASSWORD_OK));
			return;
		}
		spa.openDialog();
	}
}
