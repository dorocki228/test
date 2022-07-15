package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Warehouse.ItemClassComparator;
import l2s.gameserver.model.items.Warehouse.WarehouseType;
import l2s.gameserver.network.l2.OutgoingPackets;

public class WareHouseWithdrawListPacket extends AbstractItemPacket
{
	private final int _type;
	private final long _adena;
	private List<ItemInfo> _itemList = Collections.emptyList();
	private final int _whType;
	private final int _inventoryUsedSlots;

	public WareHouseWithdrawListPacket(int type, Player player, WarehouseType whType)
	{
		_type = type;
		_adena = player.getAdena();
		_whType = whType.ordinal();
		_inventoryUsedSlots = player.getInventory().getSize();

		ItemInstance[] items;
		switch(whType)
		{
			case PRIVATE:
				items = player.getWarehouse().getItems();
				break;
			case FREIGHT:
				items = player.getFreight().getItems();
				break;
			case CLAN:
			case CASTLE:
				items = player.getClan().getWarehouse().getItems();
				break;
			default:
				return;
		}

		_itemList = new ArrayList<ItemInfo>(items.length);
		Arrays.sort(items, ItemClassComparator.getInstance());
		for(ItemInstance item : items)
			_itemList.add(new ItemInfo(player, item));
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.WAREHOUSE_WITHDRAW_LIST.writeId(packetWriter);
		packetWriter.writeC(_type);
		if(_type == 1)
		{
			if(_whType == WarehouseType.FREIGHT.ordinal())
				packetWriter.writeH(1);
			else
				packetWriter.writeH(_whType);
			packetWriter.writeQ(_adena);
			packetWriter.writeD(_inventoryUsedSlots); //Количество занятых ячеек в инвентаре.
			packetWriter.writeD(_itemList.size());
		}
		else if(_type == 2)
		{
			packetWriter.writeH(0x00);
			/*if(_whType == WarehouseType.PRIVATE.ordinal() || _whType == WarehouseType.CLAN.ordinal())
			{
				if(_itemList.size() > 0)
					packet.writeD(_itemList.get(0).getItemId());
				else
					packet.writeD(0x00);
			}
			if(_whType == WarehouseType.CLAN.ordinal())
				packet.writeD(0x00);*/
			packetWriter.writeD(_itemList.size());
			packetWriter.writeD(_itemList.size());
			for(ItemInfo item : _itemList)
			{
				writeItem(packetWriter, item);
				packetWriter.writeD(item.getObjectId());
				packetWriter.writeD(0);
				packetWriter.writeD(0);
			}
		}

		return true;
	}
}