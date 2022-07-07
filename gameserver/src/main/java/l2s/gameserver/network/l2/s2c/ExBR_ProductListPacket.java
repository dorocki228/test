package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.data.xml.holder.ProductDataHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.item.product.ProductItem;
import l2s.gameserver.templates.item.product.ProductItemComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExBR_ProductListPacket extends L2GameServerPacket
{
	private final long _adena;
	private final boolean _history;
	private final List<ProductItem> _products;

	public ExBR_ProductListPacket(Player player, boolean history)
	{
		_products = new ArrayList<>();
		_adena = player.getAdena();
		_history = history;
		if(history)
			_products.addAll(player.getProductHistoryList().productValues());
		else
		{
			_products.addAll(ProductDataHolder.getInstance().getProductsOnSale(player));
			Collections.sort(_products);
		}
	}

	@Override
	protected void writeImpl()
	{
		writeQ(_adena);
		writeQ(0L);
        writeC(_history);
        writeD(_products.size());
		for(ProductItem product : _products)
		{
            writeD(product.getId());
            writeC(product.getCategory());
            writeC(0);
            writeD(product.getPoints(true));
            writeC(product.getTabId());
            writeD(product.getMainPageCategory());
            writeD((int) (product.getStartTimeSale() / 1000L));
            writeD((int) (product.getEndTimeSale() / 1000L));
            writeC(127);
            writeC(product.getStartHour());
            writeC(product.getStartMin());
            writeC(product.getEndHour());
            writeC(product.getEndMin());
            writeD(0);
            writeD(-1);
            writeC(product.getDiscount());
            writeC(0);
            writeC(0);
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(0);
            writeC(product.getComponents().size());
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
