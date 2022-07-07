package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ManufactureItem;
import l2s.gameserver.templates.item.RecipeTemplate;

import java.util.Collection;
import java.util.List;

public class RecipeShopManageListPacket extends L2GameServerPacket
{
	private final List<ManufactureItem> createList;
	private final Collection<RecipeTemplate> recipes;
	private final int sellerId;
	private final long adena;
	private final boolean isDwarven;

	public RecipeShopManageListPacket(Player seller, boolean isDwarvenCraft)
	{
		sellerId = seller.getObjectId();
		adena = seller.getAdena();
		isDwarven = isDwarvenCraft;
		if(isDwarven)
			recipes = seller.getDwarvenRecipeBook();
		else
			recipes = seller.getCommonRecipeBook();
		createList = seller.getCreateList();
		for(ManufactureItem mi : createList)
			if(!seller.findRecipe(mi.getRecipeId()))
				createList.remove(mi);
	}

	@Override
	protected final void writeImpl()
	{
        writeD(sellerId);
        writeD((int) Math.min(adena, 2147483647L));
        writeD(isDwarven ? 0 : 1);
        writeD(recipes.size());
		int i = 1;
		for(RecipeTemplate recipe : recipes)
		{
            writeD(recipe.getId());
            writeD(i++);
		}
        writeD(createList.size());
		for(ManufactureItem mi : createList)
		{
            writeD(mi.getRecipeId());
            writeD(0);
			writeQ(mi.getCost());
		}
	}
}
