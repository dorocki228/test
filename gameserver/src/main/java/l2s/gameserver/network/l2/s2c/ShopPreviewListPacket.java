package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.npc.BuyListTemplate;

import java.util.ArrayList;
import java.util.List;

public class ShopPreviewListPacket extends L2GameServerPacket
{
	private final int _listId;
	private final List<ItemInfo> _itemList;
	private final long _money;

	public ShopPreviewListPacket(BuyListTemplate list, Player player)
	{
		_listId = list.getListId();
		_money = player.getAdena();
		List<TradeItem> tradeList = list.getItems();
		_itemList = new ArrayList<>(tradeList.size());
		for(TradeItem item : tradeList)
			if(item.getItem().isEquipable() && (item.getItem().isArmor() || item.getItem().isWeapon()))
				_itemList.add(item);
	}

	@Override
	protected final void writeImpl()
	{
        writeD(5056);
		writeQ(_money);
        writeD(_listId);
        writeH(_itemList.size());
		for(ItemInfo item : _itemList)
			if(item.getItem().isEquipable())
			{
                writeD(item.getItemId());
                writeH(item.getItem().getType2());
                writeH(item.getItem().isEquipable() ? item.getItem().getBodyPart() : 0);
				writeQ(getWearPrice(item.getItem()));
			}
	}

	public static int getWearPrice(ItemTemplate item)
	{
		switch(item.getGrade())
		{
			case D:
			{
				return 50;
			}
			case C:
			{
				return 100;
			}
			case B:
			{
				return 200;
			}
			case A:
			{
				return 500;
			}
			case S:
			{
				return 1000;
			}
			case S80:
			{
				return 2000;
			}
			default:
			{
				return 10;
			}
		}
	}
}
