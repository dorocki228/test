package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.PrivateStoreManageList;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.TradeHelper;
import org.apache.commons.lang3.ArrayUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Это список вещей которые игрок хочет продать в создаваемом им приватном магазине
 *
 */
public class SetPrivateStoreListSell implements IClientIncomingPacket
{
	private int _count;
	private boolean _package;
	private int[] _items; // objectId
	private long[] _itemQ; // count
	private long[] _itemP; // price

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_package = packet.readD() == 1;
		_count = packet.readD();
		// Иначе нехватит памяти при создании массива.
		if(_count * 20 > packet.getReadableBytes() || _count > Short.MAX_VALUE || _count < 1)
		{
			_count = 0;
			return false;
		}

		_items = new int[_count];
		_itemQ = new long[_count];
		_itemP = new long[_count];

		for(int i = 0; i < _count; i++)
		{
			_items[i] = packet.readD();
			_itemQ[i] = packet.readQ();
			_itemP[i] = packet.readQ();
			if(_itemQ[i] < 1 || _itemP[i] < 0 || ArrayUtils.indexOf(_items, _items[i]) < i)
			{
				_count = 0;
				break;
			}
		}
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player seller = client.getActiveChar();
		if(seller == null || _count == 0)
			return;

		if(!TradeHelper.checksIfCanOpenStore(seller, _package ? Player.STORE_PRIVATE_SELL_PACKAGE : Player.STORE_PRIVATE_SELL))
		{
			seller.sendActionFailed();
			return;
		}

		TradeItem temp;
		Map<Integer, TradeItem> sellList = new LinkedHashMap<>();

		seller.getInventory().writeLock();
		try
		{
			for(int i = 0; i < _count; i++)
			{
				int objectId = _items[i];
				long count = _itemQ[i];
				long price = _itemP[i];
				ItemInstance item = seller.getInventory().getItemByObjectId(objectId);

				if(item == null || item.getCount() < count || !item.canBePrivateStore(seller) || item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
					continue;
				if(item.getPriceLimitForItem() != 0 && price > item.getPriceLimitForItem())
					price = item.getPriceLimitForItem();
				temp = new TradeItem(null, item);
				temp.setCount(count);
				temp.setOwnersPrice(price);

				sellList.put(temp.getObjectId(), temp);
			}
		}
		finally
		{
			seller.getInventory().writeUnlock();
		}

		if(sellList.size() > seller.getPrivateSellStoreLimit())
		{
			seller.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			seller.sendPacket(new PrivateStoreManageList(1, seller, _package));
			seller.sendPacket(new PrivateStoreManageList(2, seller, _package));
			return;
		}

		if(!sellList.isEmpty())
		{
			seller.setSellList(_package, sellList);
			seller.setPrivateStoreType(_package ? Player.STORE_PRIVATE_SELL_PACKAGE : Player.STORE_PRIVATE_SELL);
			seller.storePrivateStore();
			seller.broadcastPrivateStoreInfo();
			seller.sitDown(null);
			seller.broadcastCharInfo();
		}

		seller.sendActionFailed();
	}
}