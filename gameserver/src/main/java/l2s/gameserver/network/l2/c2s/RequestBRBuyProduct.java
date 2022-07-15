package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.ProductDataHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.ProductHistoryItem;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExBR_BuyProductPacket;
import l2s.gameserver.network.l2.s2c.ExBR_ExistNewProductAck;
import l2s.gameserver.network.l2.s2c.ExVipInfo;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.product.ProductItem;
import l2s.gameserver.templates.item.product.ProductItemComponent;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.loggers.ItemLogger;

import java.util.List;

public class RequestBRBuyProduct implements IClientIncomingPacket
{
	private int _productId;
	private int _count;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_productId = packet.readD();
		_count = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		if(!Config.EX_USE_PRIME_SHOP)
			return;

		Player activeChar = client.getActiveChar();

		if(activeChar == null)
			return;

		if(_count > 100 || _count <= 0)
			return;

		ProductItem product = ProductDataHolder.getInstance().getProduct(_productId);
		if(product == null)
		{
			activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_WRONG_PRODUCT);
			return;
		}

		if(!product.isOnSale() || (System.currentTimeMillis() < product.getStartTimeSale()) || (System.currentTimeMillis() > product.getEndTimeSale()))
		{
			activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_SALE_PERIOD_ENDED);
			return;
		}

		activeChar.getProductHistoryList().writeLock();
		try
		{
			if(product.getLimit() >= 0)
			{
				ProductHistoryItem productHistoryItem = activeChar.getProductHistoryList().get(product.getId());
				if(productHistoryItem != null)
					_count = Math.min(_count, product.getLimit() - productHistoryItem.getPurchasedCount());
				else
					_count = Math.min(_count, product.getLimit());

				if(_count <= 0)
				{
					activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_ITEM_LIMITED);
					return;
				}
			}

			final int pointsRequired = product.getPrice() * _count;
			if(pointsRequired <= 0 && product.getLimit() == -1) // Лимитированные вещи можно выдавать бесплатно.
			{
				activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_WRONG_PRODUCT);
				return;
			}

			activeChar.getInventory().writeLock();
			try
			{
				if(pointsRequired > activeChar.getPremiumPoints())
				{
					activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_NOT_ENOUGH_POINTS);
					return;
				}

				int totalWeight = 0;
				for(ProductItemComponent com : product.getComponents())
					totalWeight += com.getWeight();

				totalWeight *= _count; //увеличиваем вес согласно количеству

				int totalCount = 0;

				for(ProductItemComponent com : product.getComponents())
				{
					ItemTemplate item = ItemHolder.getInstance().getTemplate(com.getId());
					if(item == null)
					{
						activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_WRONG_PRODUCT);
						return; //what
					}
					totalCount += item.isStackable() ? 1 : com.getCount() * _count;
				}

				if(!activeChar.getInventory().validateCapacity(totalCount) || !activeChar.getInventory().validateWeight(totalWeight))
				{
					activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_INVENTORY_FULL);
					return;
				}

				if(pointsRequired > 0 && !activeChar.reducePremiumPoints(pointsRequired))
				{
					activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_NOT_ENOUGH_POINTS);
					return;
				}

				activeChar.getVIP().addPoints((int) (pointsRequired * activeChar.getVIP().getPointsRefillPercent() / 100.));

				activeChar.getProductHistoryList().onPurchaseProduct(product, _count);

				activeChar.sendPacket(new ExBR_ExistNewProductAck(activeChar));

				for(ProductItemComponent $comp : product.getComponents())
				{
					List<ItemInstance> items = ItemFunctions.addItem(activeChar, $comp.getId(), $comp.getCount() * _count, true);
					for(ItemInstance item : items)
						ItemLogger.INSTANCE.log(ItemLogger.ItemProcess.ItemMallBuy, activeChar, item);
				}

				activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_OK);
				activeChar.sendPacket(new ExVipInfo(activeChar));
			}
			finally
			{
				activeChar.getInventory().writeUnlock();
			}
		}
		finally
		{
			activeChar.getProductHistoryList().writeUnlock();
		}
	}
}