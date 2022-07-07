package l2s.gameserver.network.l2.s2c;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.ProductDataHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.item.product.ProductItem;
import l2s.gameserver.templates.item.product.ProductItemComponent;

import java.util.ArrayList;
import java.util.Collection;

public class ExBR_RecentProductListPacket extends L2GameServerPacket
{
	private final Collection<ProductItem> _products;

	public ExBR_RecentProductListPacket(Player activeChar)
	{
		_products = new ArrayList<>();
		int[] products = activeChar.getRecentProductList();
		if(products != null)
			for(int productId : products)
			{
				ProductItem product = ProductDataHolder.getInstance().getProduct(productId);
				if(product != null)
					_products.add(product);
			}
	}

	@Override
	protected void writeImpl()
	{
        writeD(1);
        writeD(_products.size());
		for(ProductItem product : _products)
		{
            writeD(product.getId());
            writeH(product.getCategory());
            writeD(product.getPoints(true));
            writeD(product.getTabId());
            writeD(Rnd.get(0, 4));
            writeD((int) (product.getStartTimeSale() / 1000L));
            writeD((int) (product.getEndTimeSale() / 1000L));
            writeC(127);
            writeC(product.getStartHour());
            writeC(product.getStartMin());
            writeC(product.getEndHour());
            writeC(product.getEndMin());
            writeD(0);
            writeD(-1);
            writeD(product.getDiscount());
            writeD(product.getComponents().size());
			for(ProductItemComponent component : product.getComponents())
			{
                writeD(component.getId());
                writeD((int) component.getCount());
                writeD(component.getWeight());
                writeD(component.isDropable() ? 1 : 0);
			}
		}
	}
}
