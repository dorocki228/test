package l2s.gameserver.data.xml.holder;

import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.item.RecipeTemplate;

import java.util.ArrayList;
import java.util.Collection;

public final class RecipeHolder extends AbstractHolder
{
	private static final RecipeHolder _instance;
	private final TIntObjectHashMap<RecipeTemplate> _listByRecipeId;
	private final TIntObjectHashMap<RecipeTemplate> _listByRecipeItem;

	public RecipeHolder()
	{
		_listByRecipeId = new TIntObjectHashMap<>();
		_listByRecipeItem = new TIntObjectHashMap<>();
	}

	public static RecipeHolder getInstance()
	{
		return _instance;
	}

	public void addRecipe(RecipeTemplate recipe)
	{
		_listByRecipeId.put(recipe.getId(), recipe);
		_listByRecipeItem.put(recipe.getItemId(), recipe);
	}

	public RecipeTemplate getRecipeByRecipeId(int id)
	{
		return _listByRecipeId.get(id);
	}

	public RecipeTemplate getRecipeByRecipeItem(int id)
	{
		return _listByRecipeItem.get(id);
	}

	public Collection<RecipeTemplate> getRecipes()
	{
		Collection<RecipeTemplate> result = new ArrayList<>(size());
		for(int key : _listByRecipeId.keys())
			result.add(_listByRecipeId.get(key));
		return result;
	}

	@Override
	public int size()
	{
		return _listByRecipeId.size();
	}

	@Override
	public void clear()
	{
		_listByRecipeId.clear();
		_listByRecipeItem.clear();
	}

	static
	{
		_instance = new RecipeHolder();
	}
}
