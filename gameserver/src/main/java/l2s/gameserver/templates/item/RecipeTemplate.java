package l2s.gameserver.templates.item;

import l2s.commons.util.Rnd;
import l2s.gameserver.templates.item.data.ChancedItemData;
import l2s.gameserver.templates.item.data.ItemData;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collection;

public final class RecipeTemplate
{
	private final int _id;
	private final int _level;
	private final int _mpConsume;
	private final int _successRate;
	private final int _itemId;
	private final boolean _isCommon;
	private final Collection<ItemData> _materials;
	private final Collection<ChancedItemData> _products;
	private final Collection<ItemData> _npcFee;

	public RecipeTemplate(int id, int level, int mpConsume, int successRate, int itemId, boolean isCommon)
	{
		_materials = new ArrayList<>();
		_products = new ArrayList<>();
		_npcFee = new ArrayList<>();
		_id = id;
		_level = level;
		_mpConsume = mpConsume;
		_successRate = successRate;
		_itemId = itemId;
		_isCommon = isCommon;
	}

	public int getId()
	{
		return _id;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getMpConsume()
	{
		return _mpConsume;
	}

	public int getSuccessRate()
	{
		return _successRate;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public boolean isCommon()
	{
		return _isCommon;
	}

	public void addMaterial(ItemData material)
	{
		_materials.add(material);
	}

	public ItemData[] getMaterials()
	{
		return _materials.toArray(new ItemData[0]);
	}

	public void addProduct(ChancedItemData product)
	{
		_products.add(product);
	}

	public ChancedItemData[] getProducts()
	{
		return _products.toArray(new ChancedItemData[0]);
	}

	public ChancedItemData getRandomProduct()
	{
		int chancesAmount = 0;
		for(ChancedItemData product : _products)
			chancesAmount += (int) product.getChance();
		if(Rnd.chance(chancesAmount))
		{
			ChancedItemData[] successProducts = new ChancedItemData[0];
			while(successProducts.length == 0)
				for(ChancedItemData product2 : _products)
					if(Rnd.chance(product2.getChance()))
						successProducts = ArrayUtils.add(successProducts, product2);
			return successProducts[Rnd.get(successProducts.length)];
		}
		return null;
	}

	public void addNpcFee(ItemData fee)
	{
		_npcFee.add(fee);
	}

	public ItemData[] getNpcFee()
	{
		return _npcFee.toArray(new ItemData[0]);
	}
}
