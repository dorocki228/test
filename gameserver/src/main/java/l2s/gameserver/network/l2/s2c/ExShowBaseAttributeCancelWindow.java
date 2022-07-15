package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.AttributeType;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.templates.item.ItemTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SYS
 */
public class ExShowBaseAttributeCancelWindow implements IClientOutgoingPacket
{
	private final List<ItemInstance> _items = new ArrayList<ItemInstance>();

	public ExShowBaseAttributeCancelWindow(Player activeChar)
	{
		for(ItemInstance item : activeChar.getInventory().getItems())
		{
			if(item.getAttributeElement() == AttributeType.NONE || !item.canBeEnchanted() || getAttributeRemovePrice(item) == 0)
				continue;
			_items.add(item);
		}
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_BASE_ATTRIBUTE_CANCEL_WINDOW.writeId(packetWriter);
		packetWriter.writeD(_items.size());
		for(ItemInstance item : _items)
		{
			packetWriter.writeD(item.getObjectId());
			packetWriter.writeQ(getAttributeRemovePrice(item));
		}

		return true;
	}

	public static long getAttributeRemovePrice(ItemInstance item)
	{
		switch(item.getGrade())
		{
			case S:
				return item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON ? 50000 : 40000;
			case S80:
				return item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON ? 100000 : 80000;
			case S84:
				return item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON ? 200000 : 160000;
			case R:
				return item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON ? 250000 : 240000;
			case R95:
				return item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON ? 300000 : 280000;
			case R99:
				return item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON ? 350000 : 320000;

		}
		return 0;
	}
}