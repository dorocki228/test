package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author VISTALL
 * @date 4:20/06.05.2011
 */
public class ExGMViewQuestItemListPacket extends AbstractItemPacket
{
	private final int _type;
	private int _size;
	private ItemInstance[] _items;

	private int _limit;
	private Player player;

	public ExGMViewQuestItemListPacket(int type, Player player, ItemInstance[] items, int size)
	{
		_type = type;
		_items = items;
		_size = size;
		this.player = player;
		_limit = Config.QUEST_INVENTORY_MAXIMUM;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_GM_VIEW_QUEST_ITEMLIST.writeId(packetWriter);
		packetWriter.writeC(_type);
		if(_type == 1)
		{
			packetWriter.writeS(player.getName());
			packetWriter.writeD(_limit);
			packetWriter.writeD(_size);
		}
		else if(_type == 2)
		{
			packetWriter.writeD(_size);
			packetWriter.writeD(_size);
			for(ItemInstance temp : _items)
			{
				if(temp.getTemplate().isQuest())
					writeItem(packetWriter, player, temp);
			}
		}

		return true;
	}
}
