package l2s.gameserver.model.items;

public class ManufactureItem
{
	private final int _recipeId;
	private final long _cost;

	public ManufactureItem(int recipeId, long cost)
	{
		_recipeId = recipeId;
		_cost = cost;
	}

	public int getRecipeId()
	{
		return _recipeId;
	}

	public long getCost()
	{
		return _cost;
	}

	@Override
	public boolean equals(Object o)
	{
		return o == this || o != null && o.getClass() == getClass() && ((ManufactureItem) o).getRecipeId() == getRecipeId();
	}

	@Override
	public int hashCode()
	{
		return 17 * _recipeId + 11021;
	}
}
