package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;

import java.util.ArrayList;
import java.util.List;

public class ExReplyPostItemList extends L2GameServerPacket
{
	private final List<ItemInfo> _itemsList;

	public ExReplyPostItemList(Player activeChar)
	{
		_itemsList = new ArrayList<>();
		ItemInstance[] items2;
		ItemInstance[] items = items2 = activeChar.getInventory().getItems();
		for(ItemInstance item : items2)
			if(item.canBeTraded(activeChar))
				_itemsList.add(new ItemInfo(item, item.getTemplate().isBlocked(activeChar, item)));
	}

	@Override
	protected void writeImpl()
	{
        writeD(_itemsList.size());
		for(ItemInfo item : _itemsList)
            writeItemInfo(item);
	}
}
