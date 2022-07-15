package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.OutgoingPackets;

public class GMViewWarehouseWithdrawListPacket extends AbstractItemPacket
{
	private final int _type;
	private final ItemInstance[] _items;
	@org.jetbrains.annotations.NotNull
	private final Player player;
	private String _charName;
	private long _charAdena;

	public GMViewWarehouseWithdrawListPacket(int type, Player player)
	{
		_type = type;
		_charName = player.getName();
		_charAdena = player.getWarehouse().getAdena();
		_items = player.getWarehouse().getItems();
		this.player = player;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.GM_VIEW_WAREHOUSE_WITHDRAW_LIST.writeId(packetWriter);
		packetWriter.writeC(_type);
		if(_type == 1)
		{
			packetWriter.writeS(_charName);
			packetWriter.writeQ(_charAdena);
			packetWriter.writeD(_items.length);
		}
		else if(_type == 2)
		{
			packetWriter.writeD(_items.length);
			packetWriter.writeD(_items.length);
			for(ItemInstance temp : _items)
			{
				writeItem(packetWriter, player, temp);
				packetWriter.writeD(temp.getObjectId());
			}
		}

		return true;
	}
}