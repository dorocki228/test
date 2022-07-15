package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExBR_ExistNewProductAck;

/**
 * @author Bonux
**/
public class RequestBR_NewIConCashBtnWnd implements IClientIncomingPacket
{
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		final Player player = client.getActiveChar();
		if(player == null)
			return;

		player.sendPacket(new ExBR_ExistNewProductAck(player));
	}
}