package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.HennaHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.HennaItemInfoPacket;
import l2s.gameserver.templates.HennaTemplate;

public class RequestHennaItemInfo implements IClientIncomingPacket
{
	// format  cd
	private int _symbolId;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_symbolId = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player player = client.getActiveChar();
		if(player == null)
			return;

		HennaTemplate template = HennaHolder.getInstance().getHenna(_symbolId);
		if(template != null)
			player.sendPacket(new HennaItemInfoPacket(template, player));
	}
}