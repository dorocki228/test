package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.npc.BuyListTemplate;

public class ShopPreviewListPacket implements IClientOutgoingPacket
{
	private final int _listId;
	private final List<ItemInfo> _itemList;
	private final long _money;

	public ShopPreviewListPacket(BuyListTemplate list, Player player)
	{
		_listId = list.getListId();
		_money = player.getAdena();
		List<TradeItem> tradeList = list.getItems();
		_itemList = new ArrayList<ItemInfo>(tradeList.size());
		for(TradeItem item : tradeList)
		{
			if(item.getItem().isEquipable() && (item.getItem().isArmor() || item.getItem().isWeapon())) // TODO: [Bonux] Проверить.
				_itemList.add(item);
		}
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.SHOP_PREVIEW_LIST.writeId(packetWriter);
		packetWriter.writeD(0x13c0); //?
		packetWriter.writeQ(_money);
		packetWriter.writeD(_listId);
		packetWriter.writeH(_itemList.size());

		for(ItemInfo item : _itemList)
			if(item.getItem().isEquipable())
			{
				packetWriter.writeD(item.getItemId());
				packetWriter.writeH(item.getItem().getType2()); // item type2
				packetWriter.writeQ(item.getItem().isEquipable() ? item.getItem().getBodyPart() : 0x00);
				packetWriter.writeQ(getWearPrice(item.getItem()));
			}

		return true;
	}

	public static int getWearPrice(ItemTemplate item)
	{
		switch(item.getGrade())
		{
			case D:
				return 50;
			case C:
				return 100;
				//TODO: Не известно сколько на оффе стоит примерка B - S84 ранга.
			case B:
				return 200;
			case A:
				return 500;
			case S:
				return 1000;
			case S80:
				return 2000;
				/*case S84:
					return 2500;*/
			default:
				return 10;
		}
	}
}