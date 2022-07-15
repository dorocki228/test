package l2s.gameserver.network.l2.c2s;

import l2s.commons.math.SafeMath;
import l2s.gameserver.data.string.ItemNameHolder;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.PrivateStoreBuyManageList;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.TradeHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SetPrivateStoreListBuy implements IClientIncomingPacket
{
	private static class BuyItemInfo
	{
		public int id;
		public long count;
		public long price;
		public int enchant_level;
	}

	private List<BuyItemInfo> _items = Collections.emptyList();

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		final int count = packet.readD();
		if(count * 40 > packet.getReadableBytes() || count > Short.MAX_VALUE || count < 1)
			return false;

		_items = new ArrayList<BuyItemInfo>();

		for(int i = 0; i < count; i++)
		{
			BuyItemInfo item = new BuyItemInfo();
			item.id = packet.readD();
			item.enchant_level = packet.readD();
			item.count = packet.readQ();
			item.price = packet.readQ();

			if(item.count < 1 || item.price < 1)
				break;

			packet.readD(); // Variation 1
			packet.readD(); // Variation 2

			packet.readH(); // Attack element
			packet.readH(); // Attack element power
			packet.readH(); // Fire defense
			packet.readH(); // Water defense
			packet.readH(); // Wind defense
			packet.readH(); // Earth defense
			packet.readH(); // Holy defense
			packet.readH(); // Dark defense
			packet.readD(); // Visible ID

			int saCount = packet.readC();
			for(int s = 0; s < saCount; s++)
				packet.readD(); // TODO[UNDERGROUND]: SA 1 Abnormal

			saCount = packet.readC();
			for(int s = 0; s < saCount; s++)
				packet.readD(); // TODO[UNDERGROUND]: SA 2 Abnormal

			_items.add(item);
		}
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player buyer = client.getActiveChar();
		if(buyer == null || _items.isEmpty())
			return;

		if(!TradeHelper.checksIfCanOpenStore(buyer, Player.STORE_PRIVATE_BUY))
		{
			buyer.sendActionFailed();
			return;
		}

		List<TradeItem> buyList = new CopyOnWriteArrayList<TradeItem>();
		long totalCost = 0;
		try
		{
			loop: for(BuyItemInfo i : _items)
			{
				ItemTemplate item = ItemHolder.getInstance().getTemplate(i.id);
				if(item == null || i.id == ItemTemplate.ITEM_ID_ADENA)
					continue;

				if(item.getReferencePrice() / 2 > i.price)
				{
					buyer.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.SetPrivateStoreListBuy.TooLowPrice").addString(ItemNameHolder.getInstance().getItemName(buyer, i.id)).addNumber(item.getReferencePrice() / 2));
					continue;
				}

				if(item.isStackable())
				{
					for(TradeItem bi : buyList)
					{
						if(bi.getItemId() == i.id)
						{
							bi.setOwnersPrice(i.price);
							bi.setCount(bi.getCount() + i.count);
							totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(i.count, i.price));
							continue loop;
						}
					}
				}

				TradeItem bi = new TradeItem();
				bi.setItemId(i.id);
				bi.setCount(i.count);
				bi.setOwnersPrice(i.price);
				bi.setEnchantLevel(i.enchant_level);
				totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(i.count, i.price));
				buyList.add(bi);
			}
		}
		catch(ArithmeticException ae)
		{
			//TODO audit
			buyer.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}

		if(buyList.size() > buyer.getPrivateBuyStoreLimit())
		{
			buyer.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			buyer.sendPacket(new PrivateStoreBuyManageList(1, buyer));
			buyer.sendPacket(new PrivateStoreBuyManageList(2, buyer));
			return;
		}

		if(totalCost > buyer.getAdena())
		{
			buyer.sendPacket(SystemMsg.THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE);
			buyer.sendPacket(new PrivateStoreBuyManageList(1, buyer));
			buyer.sendPacket(new PrivateStoreBuyManageList(2, buyer));
			return;
		}

		if(!buyList.isEmpty())
		{
			buyer.setBuyList(buyList);
			buyer.setPrivateStoreType(Player.STORE_PRIVATE_BUY);
			buyer.storePrivateStore();
			buyer.broadcastPrivateStoreInfo();
			buyer.sitDown(null);
			buyer.broadcastCharInfo();
		}

		buyer.sendActionFailed();
	}
}