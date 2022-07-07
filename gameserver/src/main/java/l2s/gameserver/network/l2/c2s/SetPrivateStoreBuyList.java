package l2s.gameserver.network.l2.c2s;

import l2s.commons.math.SafeMath;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.PrivateStoreBuyManageList;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.TradeHelper;
import org.apache.logging.log4j.message.ParameterizedMessage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SetPrivateStoreBuyList extends L2GameClientPacket
{
	private int _count;
	private int[] _items; // item id
	private long[] _itemQ; // count
	private long[] _itemP; // price

	@Override
	protected void readImpl()
	{
		_count = readD();
		if(_count * 40 > _buf.remaining() || _count > Short.MAX_VALUE || _count < 1)
		{
			_count = 0;
			return;
		}

		_items = new int[_count];
		_itemQ = new long[_count];
		_itemP = new long[_count];

        for(int i = 0; i < _count; i++)
		{
			_items[i] = readD();

			readH();
			readH();

			_itemQ[i] = readQ();
			_itemP[i] = readQ();

			if(_itemQ[i] < 1L || _itemP[i] < 1L)
			{
				_count = 0;
				break;
			}

			readD(); // Augmentation effect (1)
			readD(); // Augmentation effect (2)

			// elements
			readH();
			readH();
			readH();
			readH();
			readH();
			readH();
			readH();
			readH();

			readD(); // Visible item

			readC();
			readC();
		}
	}

	@Override
	protected void runImpl()
	{
		Player buyer = getClient().getActiveChar();
		if(buyer == null || _count == 0)
			return;

		if(!TradeHelper.checksIfCanOpenStore(buyer, Player.STORE_PRIVATE_BUY))
		{
			buyer.sendActionFailed();
			return;
		}

		List<TradeItem> buyList = new CopyOnWriteArrayList<>();
		long totalCost = 0L;
        int slots = 0;
        long weight = 0;
		try
		{
            loop: for(int i = 0; i < _count; i++)
			{
				int itemId = _items[i];
				long count = _itemQ[i];
				long price = _itemP[i];

				ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
                if(item == null)
                {
                    String messagePattern = "SetPrivateStoreBuyList: Can't find item. Player {} itemId {} count {} price {}";
                    ParameterizedMessage message = new ParameterizedMessage(messagePattern, buyer, itemId, count, price);
                    LogService.getInstance().log(LoggerType.ILLEGAL_ACTIONS, message);

                    continue;
                }
                if(itemId == ItemTemplate.ITEM_ID_ADENA)
                {
                    continue;
                }

                if(item.isStackable())
                    for(TradeItem bi : buyList)
                        if(bi.getItemId() == itemId)
                        {
                            bi.setOwnersPrice(price);
                            bi.setCount(bi.getCount() + count);
                            totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
                            continue loop;
                        }

                TradeItem bi2 = new TradeItem();
                bi2.setItemId(itemId);
                bi2.setCount(count);
                bi2.setOwnersPrice(price);
                totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
                weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, item.getWeight()));
                if(!item.isStackable() || buyer.getInventory().getItemByItemId(item.getItemId()) == null)
                    slots++;
                buyList.add(bi2);
            }
		}
		catch(ArithmeticException ae)
		{
			buyer.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}

		if(buyList.size() > buyer.getTradeLimit())
		{
			buyer.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			buyer.sendPacket(new PrivateStoreBuyManageList(buyer));
			return;
		}

		if(totalCost > buyer.getAdena())
		{
			buyer.sendPacket(SystemMsg.THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE);
			buyer.sendPacket(new PrivateStoreBuyManageList(buyer));
			return;
		}

        if(!buyer.getInventory().validateWeight(weight) || !buyer.getInventory().validateCapacity(slots))
        {
            buyer.sendPacket(SystemMsg.THE_WEIGHT_AND_VOLUME_LIMIT_OF_YOUR_INVENTORY_CANNOT_BE_EXCEEDED);
            buyer.sendPacket(new PrivateStoreBuyManageList(buyer));
            return;
        }

		if(!buyList.isEmpty())
		{
			buyer.setBuyList(buyList);
			buyer.saveTradeList();
			buyer.setPrivateStoreType(Player.STORE_PRIVATE_BUY);
			buyer.broadcastPrivateStoreInfo();
			buyer.sitDown(null);
			buyer.broadcastCharInfo();
		}

		buyer.sendActionFailed();
	}
}
