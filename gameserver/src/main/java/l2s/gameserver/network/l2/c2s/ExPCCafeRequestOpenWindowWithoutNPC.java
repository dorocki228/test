package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.HtmlMessage;

/**
 * @author Bonux
**/
public class ExPCCafeRequestOpenWindowWithoutNPC implements IClientIncomingPacket
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

		HtmlMessage html = new HtmlMessage(5);
		html.setFile("pc_bang_shop.htm");
		activeChar.sendPacket(html);
	}
}