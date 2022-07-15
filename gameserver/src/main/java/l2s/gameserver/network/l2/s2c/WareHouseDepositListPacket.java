package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Warehouse.ItemClassComparator;
import l2s.gameserver.model.items.Warehouse.WarehouseType;
import l2s.gameserver.network.l2.OutgoingPackets;

public class WareHouseDepositListPacket extends AbstractItemPacket
{
	private final int _type;
	private final int _whtype;
	private final long _adena;
	private final List<ItemInfo> _itemList;
	private final int _depositedItemsCount;

	public WareHouseDepositListPacket(int type, Player cha, WarehouseType whtype)
	{
		_type = type;
		_whtype = whtype.ordinal();
		_adena = cha.getAdena();

		ItemInstance[] items = cha.getInventory().getItems();
		Arrays.sort(items, ItemClassComparator.getInstance());
		_itemList = new ArrayList<ItemInfo>(items.length);
		for(ItemInstance item : items)
			if(item.canBeStored(cha, _whtype == 1))
				_itemList.add(new ItemInfo(cha, item, item.getTemplate().isBlocked(cha, item)));

		switch(whtype)
		{
			case PRIVATE:
				_depositedItemsCount = cha.getWarehouse().getSize();
				break;
			case FREIGHT:
				_depositedItemsCount = cha.getFreight().getSize();
				break;
			case CLAN:
			case CASTLE:
				_depositedItemsCount = cha.getClan().getWarehouse().getSize();
				break;
			default:
				_depositedItemsCount = 0;
				return;
		}
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.WAREHOUSE_DEPOSIT_LIST.writeId(packetWriter);
		packetWriter.writeC(_type);
		packetWriter.writeH(_whtype);
		if(_type == 1)
		{
			packetWriter.writeQ(_adena);
			packetWriter.writeH(_depositedItemsCount); //Количество вещей которые уже есть в банке.
			packetWriter.writeD(0x00);
			packetWriter.writeD(0x00);
		}
		else if(_type == 2)
		{
			packetWriter.writeH(0);//TODO [Bonux]
			packetWriter.writeD(_itemList.size());
			for(ItemInfo item : _itemList)
			{
				writeItem(packetWriter, item);
				packetWriter.writeD(item.getObjectId());
			}
		}

		return true;
	}
}