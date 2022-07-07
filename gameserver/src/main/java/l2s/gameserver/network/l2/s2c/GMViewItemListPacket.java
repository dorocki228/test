package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;

public class GMViewItemListPacket extends L2GameServerPacket
{
	private final int _size;
	private final ItemInstance[] _items;
	private final int _limit;
	private final String _name;
	private final Player _player;

	public GMViewItemListPacket(Player cha, ItemInstance[] items, int size)
	{
		_size = size;
		_items = items;
		_name = cha.getName();
		_limit = cha.getInventoryLimit();
		_player = cha;
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_name);
        writeD(_limit);
        writeH(1);
        writeH(_size);
		for(ItemInstance temp : _items)
			if(!temp.getTemplate().isQuest())
                writeItemInfo(_player, temp);
	}
}
