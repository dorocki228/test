package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.Inventory;

import java.util.Map;

public class ShopPreviewInfoPacket extends L2GameServerPacket
{
	private final Map<Integer, Integer> _itemlist;

	public ShopPreviewInfoPacket(Map<Integer, Integer> itemlist)
	{
		_itemlist = itemlist;
	}

	@Override
	protected void writeImpl()
	{
        writeD(33);
		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
            writeD(getFromList(PAPERDOLL_ID));
	}

	private int getFromList(int key)
	{
		return _itemlist.get(key) != null ? _itemlist.get(key) : 0;
	}
}
