package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.MultiSellListContainer;
import l2s.gameserver.model.base.MultiSellEntry;
import l2s.gameserver.model.base.MultiSellIngredient;
import l2s.gameserver.templates.item.ItemTemplate;

import java.util.ArrayList;
import java.util.List;

public class MultiSellListPacket extends L2GameServerPacket
{
	private final int _page;
	private final int _finished;
	private final int _listId;
	private final int _type;
	private final List<MultiSellEntry> _list;

	public MultiSellListPacket(MultiSellListContainer list, int page, int finished)
	{
		_list = list.getEntries();
		_listId = list.getListId();
		_type = list.getType().ordinal();
		_page = page;
		_finished = finished;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0);
		writeD(_listId);
		writeC(0);
		writeD(_page);
		writeD(_finished);
		writeD(Config.MULTISELL_SIZE);
		writeD(_list.size());
		writeC(_type);
		writeD(0);
		for(MultiSellEntry ent : _list)
		{
			List<MultiSellIngredient> ingredients = fixIngredients(ent.getIngredients());
			writeD(ent.getEntryId());
			writeC(!ent.getProduction().isEmpty() && ent.getProduction().get(0).isStackable() ? 1 : 0);
			// TODO item
			writeH(0);
			writeD(0);
			writeD(0);
			writeItemElements();

			int saCount = 0;
			writeC(saCount);
			for(int i = 0; i < saCount; ++i)
				writeD(0);

			writeC(saCount);
			for(int i = 0; i < saCount; ++i)
				writeD(0);

			writeH(ent.getProduction().size());
			writeH(ingredients.size());
			for(MultiSellIngredient prod : ent.getProduction())
			{
				int itemId = prod.getItemId();
				ItemTemplate template = itemId > 0 ? ItemHolder.getInstance().getTemplate(prod.getItemId()) : null;
				writeD(itemId);
				writeQ(itemId > 0 ? (long) template.getBodyPart() : 0L);
				writeH(itemId > 0 ? template.getType2() : 0);
				writeQ(prod.getItemCount());
				writeH(prod.getItemEnchant());
				writeD(prod.getChance());
				writeD(prod.getItemAugmentations()[0]);
				writeD(prod.getItemAugmentations()[1]);
				writeItemElements(prod);

				// TODO Ensoul Options
				int productionSaCount = 0;
				writeC(productionSaCount);
				for(int i = 0; i < productionSaCount; ++i)
					writeD(0);

				writeC(productionSaCount);
				for(int i = 0; i < productionSaCount; ++i)
					writeD(0);
			}

			for(MultiSellIngredient i : ingredients)
			{
				int itemId = i.getItemId();
				ItemTemplate item = itemId > 0 ? ItemHolder.getInstance().getTemplate(i.getItemId()) : null;
				writeD(itemId);
				writeH(itemId > 0 ? item.getType2() : 65535);
				writeQ(i.getItemCount());
				writeH(i.getItemEnchant());
				writeD(i.getItemAugmentations()[0]);
				writeD(i.getItemAugmentations()[1]);
				writeItemElements(i);

				int ingredientSaCount = 0;
				writeC(ingredientSaCount);
				for(int ii = 0; ii < ingredientSaCount; ++ii)
					writeD(0);

				writeC(ingredientSaCount);
				for(int ii = 0; ii < ingredientSaCount; ++ii)
					writeD(0);
			}
		}
	}

	private static List<MultiSellIngredient> fixIngredients(List<MultiSellIngredient> ingredients)
	{
		int needFix = 0;
		for(MultiSellIngredient ingredient : ingredients)
			if(ingredient.getItemCount() > 2147483647L)
				++needFix;
		if(needFix == 0)
			return ingredients;
		List<MultiSellIngredient> result = new ArrayList<>(ingredients.size() + needFix);
		for(MultiSellIngredient ingredient2 : ingredients)
		{
			ingredient2 = ingredient2.clone();
			while(ingredient2.getItemCount() > 2147483647L)
			{
				MultiSellIngredient temp = ingredient2.clone();
				temp.setItemCount(2000000000L);
				result.add(temp);
				ingredient2.setItemCount(ingredient2.getItemCount() - 2000000000L);
			}
			if(ingredient2.getItemCount() > 0L)
				result.add(ingredient2);
		}
		return result;
	}
}
