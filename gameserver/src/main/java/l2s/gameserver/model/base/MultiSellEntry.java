package l2s.gameserver.model.base;

import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.templates.item.ItemTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MultiSellEntry
{
	private int _entryId;
	private ItemInfo itemInfo;
	private List<MultiSellIngredient> _ingredients = new ArrayList<MultiSellIngredient>();
	private List<MultiSellIngredient> _production = new ArrayList<MultiSellIngredient>();
	private long _tax;

	public MultiSellEntry()
	{}

	public MultiSellEntry(int id)
	{
		_entryId = id;
	}

	public MultiSellEntry(int id, ItemInfo itemInfo)
	{
		_entryId = id;
		this.itemInfo = itemInfo;
	}

	/**
	 * @param entryId The entryId to set.
	 */
	public void setEntryId(int entryId)
	{
		_entryId = entryId;
	}

	/**
	 * @return Returns the entryId.
	 */
	public int getEntryId()
	{
		return _entryId;
	}

	public ItemInfo getItemInfo() {
		return itemInfo;
	}

	/**
	 * @param ingredients The ingredients to set.
	 */
	public void addIngredient(MultiSellIngredient ingredient)
	{
		_ingredients.add(ingredient);
	}

	/**
	 * @return Returns the ingredients.
	 */
	public List<MultiSellIngredient> getIngredients()
	{
		return _ingredients;
	}

	/**
	 * @param ingredients The ingredients to set.
	 */
	public void addProduct(MultiSellIngredient ingredient)
	{
		_production.add(ingredient);
	}

	/**
	 * @return Returns the ingredients.
	 */
	public List<MultiSellIngredient> getProduction()
	{
		return _production;
	}

	public final boolean isStackable()
	{
		return _production.stream().map(i -> ItemHolder.getInstance().getTemplate(i.getItemId()))
				.filter(Objects::nonNull)
				.allMatch(ItemTemplate::isStackable);
	}

	public long getTax()
	{
		return _tax;
	}

	public void setTax(long tax)
	{
		_tax = tax;
	}

	@Override
	public int hashCode()
	{
		return _entryId;
	}

	@Override
	public MultiSellEntry clone()
	{
		MultiSellEntry ret = new MultiSellEntry(_entryId);
		for(MultiSellIngredient i : _ingredients)
			ret.addIngredient(i.clone());
		for(MultiSellIngredient i : _production)
			ret.addProduct(i.clone());
		return ret;
	}
}