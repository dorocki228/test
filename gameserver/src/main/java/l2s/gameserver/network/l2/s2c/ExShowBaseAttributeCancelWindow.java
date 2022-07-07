package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.items.ItemInstance;

import java.util.ArrayList;
import java.util.List;

public class ExShowBaseAttributeCancelWindow extends L2GameServerPacket
{
	private final List<ItemInstance> _items;

	public ExShowBaseAttributeCancelWindow(Player activeChar)
	{
		_items = new ArrayList<>();
		for(ItemInstance item : activeChar.getInventory().getItems())
			if(item.getAttributeElement() != Element.NONE && item.canBeEnchanted())
				if(getAttributeRemovePrice(item) != 0L)
					_items.add(item);
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_items.size());
		for(ItemInstance item : _items)
		{
            writeD(item.getObjectId());
			writeQ(getAttributeRemovePrice(item));
		}
	}

	public static long getAttributeRemovePrice(ItemInstance item)
	{
		switch(item.getGrade())
		{
			case S:
			{
				return item.getTemplate().getType2() == 0 ? 50000L : 40000L;
			}
			case S80:
			{
				return item.getTemplate().getType2() == 0 ? 100000L : 80000L;
			}
			case S84:
			{
				return item.getTemplate().getType2() == 0 ? 200000L : 160000L;
			}
			case R:
			{
				return item.getTemplate().getType2() == 0 ? 250000L : 240000L;
			}
			case R95:
			{
				return item.getTemplate().getType2() == 0 ? 300000L : 280000L;
			}
			case R99:
			{
				return item.getTemplate().getType2() == 0 ? 350000L : 320000L;
			}
			default:
			{
				return 0L;
			}
		}
	}
}
