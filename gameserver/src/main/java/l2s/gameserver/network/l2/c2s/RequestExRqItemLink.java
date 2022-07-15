package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.ItemInfoCache;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.ExRpItemLink;

public class RequestExRqItemLink implements IClientIncomingPacket
{
	private int _objectId;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_objectId = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player player = client.getActiveChar();
		if(player == null)
			return;

		ItemInfo item;
		if((item = ItemInfoCache.getInstance().get(_objectId)) == null)
			player.sendPacket(ActionFailPacket.STATIC);
		else
			player.sendPacket(new ExRpItemLink(item));
	}
}