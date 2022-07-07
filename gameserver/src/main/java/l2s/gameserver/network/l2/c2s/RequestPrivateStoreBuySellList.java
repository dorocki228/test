package l2s.gameserver.network.l2.c2s;

import l2s.commons.math.SafeMath;
import l2s.gameserver.logging.ItemLogProcess;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.logging.message.ItemLogMessage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.TradeHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RequestPrivateStoreBuySellList extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestPrivateStoreBuySellList.class);
	private int _buyerId;
	private int _count;
	private int[] _items;
	private long[] _itemQ;
	private long[] _itemP;

	@Override
	protected void readImpl()
	{
		_buyerId = readD();
		_count = readD();
		if(_count * 28 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		_itemQ = new long[_count];
		_itemP = new long[_count];
		for(int i = 0; i < _count; ++i)
		{
			_items[i] = readD();
			readD();
			readH();
			readH();
			_itemQ[i] = readQ();
			_itemP[i] = readQ();
			readD();
			if(_itemQ[i] < 1L || _itemP[i] < 1L || ArrayUtils.indexOf(_items, _items[i]) < i)
			{
				_count = 0;
				break;
			}
		}
	}

	@Override
	protected void runImpl()
	{
		Player seller = getClient().getActiveChar();
		if(seller == null || _count == 0)
			return;

		if(seller.isActionsDisabled())
		{
			seller.sendActionFailed();
			return;
		}

		if(seller.isInStoreMode())
		{
			seller.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(seller.isInTrade())
		{
			seller.sendActionFailed();
			return;
		}

		if(seller.isFishing())
		{
			seller.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_);
			return;
		}

		Player buyer = (Player) seller.getVisibleObject(_buyerId);
		if(buyer == null || buyer.getPrivateStoreType() != Player.STORE_PRIVATE_BUY || !seller.checkInteractionDistance(buyer))
		{
			seller.sendPacket(SystemMsg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
			seller.sendActionFailed();
			return;
		}

		List<TradeItem> buyList = buyer.getBuyList();
		if(buyList.isEmpty())
		{
			seller.sendPacket(SystemMsg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
			seller.sendActionFailed();
			return;
		}

        buyer.getInventory().writeLock();
		seller.getInventory().writeLock();
        long weight = 0;
        int slots = 0;
        long totalCost = 0;
        List<TradeItem> sellList = new ArrayList<>();
        try
		{
			loop: for(int i = 0; i < _count; i++)
			{
				int objectId = _items[i];
				long count = _itemQ[i];
				long price = _itemP[i];

				ItemInstance item = seller.getInventory().getItemByObjectId(objectId);
				if(item == null || item.getCount() < count || !item.canBePrivateStore(seller))
					break loop;

				TradeItem si = null;

				for(TradeItem bi : buyList)
					if(bi.getItemId() == item.getItemId())
						if(bi.getOwnersPrice() == price)
						{
							if(count > bi.getCount())
								break loop;

							totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
							weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, item.getTemplate().getWeight()));
							if(!item.isStackable() || buyer.getInventory().getItemByItemId(item.getItemId()) == null)
								slots++;

							si = new TradeItem();
							si.setObjectId(objectId);
							si.setItemId(item.getItemId());
							si.setCount(count);
							si.setOwnersPrice(price);

							sellList.add(si);
							break;
						}
			}
		}
		catch(ArithmeticException ae)
		{
			//TODO audit
			sellList.clear();
            seller.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
            try
            {
                if(sellList.size() != _count)
                {
                    seller.sendPacket(SystemMsg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
                    seller.sendActionFailed();
                    return;
                }
                if(!buyer.getInventory().validateWeight(weight))
                {
                    buyer.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
                    seller.sendPacket(SystemMsg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
                    seller.sendActionFailed();
                    return;
                }
                if(!buyer.getInventory().validateCapacity(slots))
                {
                    buyer.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
                    seller.sendPacket(SystemMsg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
                    seller.sendActionFailed();
                    return;
                }
                if(!buyer.reduceAdena(totalCost))
                {
                    buyer.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                    seller.sendPacket(SystemMsg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
                    seller.sendActionFailed();
                    return;
                }
                for(TradeItem si2 : sellList)
                {
                    ItemInstance item2 = seller.getInventory().removeItemByObjectId(si2.getObjectId(), si2.getCount());
                    for(TradeItem bi2 : buyList)
                        if(bi2.getItemId() == si2.getItemId() && bi2.getOwnersPrice() == si2.getOwnersPrice())
                        {
                            bi2.setCount(bi2.getCount() - si2.getCount());
                            if(bi2.getCount() < 1L)
                            {
                                buyList.remove(bi2);
                                break;
                            }
                            break;
                        }

					ItemLogMessage message = new ItemLogMessage(seller, ItemLogProcess.PrivateStoreSell, item2);
					LogService.getInstance().log(LoggerType.ITEM, message);

					message = new ItemLogMessage(buyer, ItemLogProcess.PrivateStoreBuy, item2);
					LogService.getInstance().log(LoggerType.ITEM, message);

                    buyer.getInventory().addItem(item2);
                    TradeHelper.purchaseItem(buyer, seller, si2);
                }
                long tax = TradeHelper.getTax(seller, totalCost);
                if(tax > 0L)
                    totalCost -= tax;
                seller.addAdena(totalCost);
                buyer.saveTradeList();
            }
            finally
            {
                seller.getInventory().writeUnlock();
                buyer.getInventory().writeUnlock();
            }
			return;
		}
		finally
		{
			try
			{
				if(sellList.size() != _count)
				{
					seller.sendPacket(SystemMsg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
					seller.sendActionFailed();
					return;
				}

				if(!buyer.getInventory().validateWeight(weight))
				{
					buyer.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
					seller.sendPacket(SystemMsg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
					seller.sendActionFailed();
					return;
				}

				if(!buyer.getInventory().validateCapacity(slots))
				{
					buyer.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
					seller.sendPacket(SystemMsg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
					seller.sendActionFailed();
					return;
				}

				if(!buyer.reduceAdena(totalCost))
				{
					buyer.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					seller.sendPacket(SystemMsg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
					seller.sendActionFailed();
					return;
				}

                for(TradeItem si : sellList)
				{
                    ItemInstance item = seller.getInventory().removeItemByObjectId(si.getObjectId(), si.getCount());
                    for(TradeItem bi : buyList)
						if(bi.getItemId() == si.getItemId())
							if(bi.getOwnersPrice() == si.getOwnersPrice())
							{
								bi.setCount(bi.getCount() - si.getCount());
								if(bi.getCount() < 1L)
									buyList.remove(bi);
								break;
							}

					ItemLogMessage message = new ItemLogMessage(seller, ItemLogProcess.PrivateStoreSell,
							item, item.getCount(), si.getOwnersPrice());
					LogService.getInstance().log(LoggerType.ITEM, message);

					message = new ItemLogMessage(buyer, ItemLogProcess.PrivateStoreBuy,
							item, item.getCount(), si.getOwnersPrice());
					LogService.getInstance().log(LoggerType.ITEM, message);

					buyer.getInventory().addItem(item);
					TradeHelper.purchaseItem(buyer, seller, si);
				}

				long tax = TradeHelper.getTax(seller, totalCost);
				if(tax > 0)
				{
					totalCost -= tax;
				}

				seller.addAdena(totalCost);
				buyer.saveTradeList();
			}
			finally
			{
				seller.getInventory().writeUnlock();
				buyer.getInventory().writeUnlock();
			}
		}

		if(buyList.isEmpty())
			TradeHelper.cancelStore(buyer);

		seller.sendChanges();
		buyer.sendChanges();

		seller.sendActionFailed();
	}
}
