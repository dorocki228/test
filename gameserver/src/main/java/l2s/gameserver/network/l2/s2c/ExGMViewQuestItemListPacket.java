package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;

public class ExGMViewQuestItemListPacket extends L2GameServerPacket
{
	private final int _size;
	private final ItemInstance[] _items;
	private final int _limit;
	private final String _name;

	public ExGMViewQuestItemListPacket(Player player, ItemInstance[] items, int size)
	{
		_items = items;
		_size = size;
		_name = player.getName();
		_limit = Config.QUEST_INVENTORY_MAXIMUM;
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_name);
        writeD(_limit);
        writeH(_size);
		for(ItemInstance temp : _items)
			if(temp.getTemplate().isQuest())
                writeItemInfo(temp);
	}
}
