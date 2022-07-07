package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.data.xml.holder.ProductDataHolder;
import l2s.gameserver.templates.item.product.ProductItem;
import l2s.gameserver.templates.item.product.ProductItemComponent;

public class ExBR_ProductInfoPacket extends L2GameServerPacket
{
	private final ProductItem _productId;

	public ExBR_ProductInfoPacket(int id)
	{
		_productId = ProductDataHolder.getInstance().getProduct(id);
	}

	@Override
	protected void writeImpl()
	{
		if(_productId == null)
			return;
        writeD(_productId.getId());
        writeD(_productId.getPoints(true));
        writeD(_productId.getComponents().size());
		for(ProductItemComponent com : _productId.getComponents())
		{
            writeD(com.getId());
            writeD((int) com.getCount());
            writeD(com.getWeight());
            writeD(com.isDropable() ? 1 : 0);
		}
	}
}
