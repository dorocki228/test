package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.ProductDataHolder;
import l2s.gameserver.logging.ItemLogProcess;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.logging.message.ItemLogMessage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExBR_BuyProductPacket;
import l2s.gameserver.network.l2.s2c.ExBR_GamePointPacket;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.product.ProductItem;
import l2s.gameserver.templates.item.product.ProductItemComponent;
import l2s.gameserver.utils.ItemFunctions;

import java.util.List;

public class RequestExBR_BuyProduct extends L2GameClientPacket
{
	private int _productId;
	private int _count;

	@Override
	protected void readImpl()
	{
		_productId = readD();
		_count = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(_count > 99 || _count <= 0)
			return;
		ProductItem product = ProductDataHolder.getInstance().getProduct(_productId);
		if(product == null)
		{
			activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_WRONG_PRODUCT);
			return;
		}
		if(!product.isOnSale() || System.currentTimeMillis() < product.getStartTimeSale() || System.currentTimeMillis() > product.getEndTimeSale())
		{
			activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_SALE_PERIOD_ENDED);
			return;
		}
		int pointsRequired = product.getPoints(true) * _count;
		if(pointsRequired <= 0)
		{
			activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_WRONG_PRODUCT);
			return;
		}
		long pointsCount = activeChar.getPremiumPoints();
		if(pointsRequired > pointsCount)
		{
			activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_NOT_ENOUGH_POINTS);
			return;
		}
		int totalWeight = 0;
		for(ProductItemComponent com : product.getComponents())
			totalWeight += com.getWeight();
		totalWeight *= _count;
		int totalCount = 0;
		for(ProductItemComponent com2 : product.getComponents())
		{
			ItemTemplate item = ItemHolder.getInstance().getTemplate(com2.getId());
			if(item == null)
			{
				activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_WRONG_PRODUCT);
				return;
			}
			totalCount += (int) (item.isStackable() ? 1L : com2.getCount() * _count);
		}
		if(!activeChar.getInventory().validateCapacity(totalCount) || !activeChar.getInventory().validateWeight(totalWeight))
		{
			activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_INVENTORY_FULL);
			return;
		}
		if(!activeChar.reducePremiumPoints(pointsRequired))
		{
			activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_NOT_ENOUGH_POINTS);
			return;
		}
		activeChar.getProductHistoryList().onPurchaseProduct(product);
		for(ProductItemComponent $comp : product.getComponents())
		{
			List<ItemInstance> items = ItemFunctions.addItem(activeChar, $comp.getId(), $comp.getCount() * _count, false);
			for(ItemInstance item2 : items)
			{
				ItemLogMessage message = new ItemLogMessage(activeChar, ItemLogProcess.ItemMallBuy, item2);
				LogService.getInstance().log(LoggerType.ITEM, message);
			}
		}
		activeChar.updateRecentProductList(_productId);
		activeChar.sendPacket(new ExBR_GamePointPacket(activeChar));
		activeChar.sendPacket(ExBR_BuyProductPacket.RESULT_OK);
		activeChar.sendChanges();
	}
}
