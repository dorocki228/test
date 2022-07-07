package l2s.gameserver.model.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiSellEntry
{
	private int _entryId;
	private final List<MultiSellIngredient> _ingredients;
	private final List<MultiSellIngredient> _production;
	private long _tax;
	private boolean free;

	public MultiSellEntry()
	{
		_ingredients = new ArrayList<>();
		_production = new ArrayList<>();
	}

	public MultiSellEntry(int id)
	{
		_ingredients = new ArrayList<>();
		_production = new ArrayList<>();
		_entryId = id;
	}

	public MultiSellEntry(boolean free)
	{
		_ingredients = free ? Collections.emptyList() : new ArrayList<>();
		_production = new ArrayList<>();
		this.free = free;
	}

	public MultiSellEntry(int entryId, boolean free)
	{
		_ingredients = new ArrayList<>();
		_production = new ArrayList<>();
		_entryId = entryId;
		this.free = free;
	}

	public void setEntryId(int entryId)
	{
		_entryId = entryId;
	}

	public int getEntryId()
	{
		return _entryId;
	}

	public void addIngredient(MultiSellIngredient ingredient)
	{
		_ingredients.add(ingredient);
	}

	public List<MultiSellIngredient> getIngredients()
	{
		return _ingredients;
	}

	public void addProduct(MultiSellIngredient ingredient)
	{
		_production.add(ingredient);
	}

	public List<MultiSellIngredient> getProduction()
	{
		return _production;
	}

	public long getTax()
	{
		return _tax;
	}

	public void setTax(long tax)
	{
		_tax = tax;
	}

	public boolean isFree()
	{
		return free;
	}

	@Override
	public int hashCode()
	{
		return _entryId;
	}

	@Override
	public MultiSellEntry clone()
	{
		MultiSellEntry ret = new MultiSellEntry(_entryId, free);
		for(MultiSellIngredient i : _ingredients)
			ret.addIngredient(i.clone());
		for(MultiSellIngredient i : _production)
			ret.addProduct(i.clone());
		return ret;
	}
}
