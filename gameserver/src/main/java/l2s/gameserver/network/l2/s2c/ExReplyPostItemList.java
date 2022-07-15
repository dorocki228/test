package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.network.l2.c2s.RequestPostItemList;

/**
 * Ответ на запрос создания нового письма.
 * Отсылается при получении {@link RequestPostItemList}
 * Содержит список вещей, которые можно приложить к письму.
 */
public class ExReplyPostItemList extends AbstractItemPacket
{
	private final int _type;
	private final List<ItemInfo> _itemsList = new ArrayList<ItemInfo>();

	public ExReplyPostItemList(int type, Player activeChar)
	{
		_type = type;

		ItemInstance[] items = activeChar.getInventory().getItems();
		for(ItemInstance item : items)
		{
			if(item.canBeTraded(activeChar))
				_itemsList.add(new ItemInfo(activeChar, item, item.getTemplate().isBlocked(activeChar, item)));
		}
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_REPLY_POST_ITEM_LIST.writeId(packetWriter);
		packetWriter.writeC(_type);
		packetWriter.writeD(_itemsList.size());
		if(_type == 2)
		{
			packetWriter.writeD(_itemsList.size());
			for(ItemInfo item : _itemsList)
				writeItem(packetWriter, item);
		}

		return true;
	}
}