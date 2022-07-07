package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.templates.item.RecipeTemplate;

import java.util.Collection;

public class RecipeBookItemListPacket extends L2GameServerPacket
{
	private final Collection<RecipeTemplate> _recipes;
	private final boolean _isDwarvenCraft;
	private final int _currentMp;

	public RecipeBookItemListPacket(Player player, boolean isDwarvenCraft)
	{
		_isDwarvenCraft = isDwarvenCraft;
		_currentMp = (int) player.getCurrentMp();
		if(isDwarvenCraft)
			_recipes = player.getDwarvenRecipeBook();
		else
			_recipes = player.getCommonRecipeBook();
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_isDwarvenCraft ? 0 : 1);
        writeD(_currentMp);
        writeD(_recipes.size());
		for(RecipeTemplate recipe : _recipes)
		{
            writeD(recipe.getId());
            writeD(1);
		}
	}
}
