package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class RequestSaveInventoryOrder implements IClientIncomingPacket
{
	// format: (ch)db, b - array of (dd)
	int[][] _items;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		int size = packet.readD();
		if(size > 125)
			size = 125;
		if(size * 8 > packet.getReadableBytes() || size < 1)
		{
			_items = null;
			return false;
		}
		_items = new int[size][2];
		for(int i = 0; i < size; i++)
		{
			_items[i][0] = packet.readD(); // item id
			_items[i][1] = packet.readD(); // slot
		}
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		if(_items == null)
			return;
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;
		activeChar.getInventory().sort(_items);
	}
}