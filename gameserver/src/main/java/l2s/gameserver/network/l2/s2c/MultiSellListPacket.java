package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.MultiSellListContainer;
import l2s.gameserver.model.base.MultiSellEntry;
import l2s.gameserver.model.base.MultiSellIngredient;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.templates.item.ItemTemplate;

import java.util.ArrayList;
import java.util.List;

public class MultiSellListPacket extends AbstractItemPacket
{
	private final int _page;
	private final int _finished;
	private final int _listId;
	private final int _type;
	private final boolean isKeepEnchant;
	private final List<MultiSellEntry> _list;

	public MultiSellListPacket(MultiSellListContainer list, int page, int finished)
	{
		_list = list.getEntries();
		_listId = list.getListId();
		_type = list.getType().ordinal();
		isKeepEnchant = list.isKeepEnchant();
		_page = page;
		_finished = finished;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.MULTI_SELL_LIST.writeId(packetWriter);

		packetWriter.writeC(0x00); // UNK
		packetWriter.writeD(_listId); // list id
		packetWriter.writeC(0x00); // UNK
		packetWriter.writeD(_page); // page
		packetWriter.writeD(_finished); // finished
		packetWriter.writeD(Config.MULTISELL_SIZE); // size of pages
		packetWriter.writeD(_list.size()); //list length
		packetWriter.writeC(0x00); // [TODO]: Grand Crusade
		packetWriter.writeC(_type);//Type (0x00 - Нормальный, 0xD0 - с шансом)
		packetWriter.writeD(0x00); // UNK
		List<MultiSellIngredient> ingredients;
		for(MultiSellEntry ent : _list)
		{
			// TODO need this ? ingredients = fixIngredients(ent.getIngredients());
			ingredients = ent.getIngredients();

			ItemInfo itemInfo = ent.getItemInfo();

			packetWriter.writeD(ent.getEntryId());
			packetWriter.writeC(ent.isStackable() ? 1 : 0); // stackable?
			// Those values will be passed down to MultiSellChoose packet.
			packetWriter.writeH(itemInfo != null ? itemInfo.getEnchantLevel() : 0); // enchant level
			writeItemAugment(packetWriter, itemInfo);
			writeItemElements(packetWriter, itemInfo);
			writeItemEnsoulOptions(packetWriter, itemInfo);

			packetWriter.writeH(ent.getProduction().size());
			packetWriter.writeH(ingredients.size());

			for(MultiSellIngredient prod : ent.getProduction())
			{
				int itemId = prod.getItemId();
				ItemTemplate template = itemId > 0 ? ItemHolder.getInstance().getTemplate(prod.getItemId()) : null;
				ItemInfo prodInfo = isKeepEnchant && itemInfo != null && template != null
						&& template.getClass().equals(itemInfo.getItem().getClass()) ? itemInfo : null;

				packetWriter.writeD(itemId);
				packetWriter.writeQ(itemId > 0 ? template.getBodyPart() : 0);
				packetWriter.writeH(itemId > 0 ? template.getType2() : 0xffff);
				packetWriter.writeQ(prod.getItemCount());
				packetWriter.writeH(prod.getItemEnchant());
				packetWriter.writeD(prod.getChance());
				writeItemAugment(packetWriter, prodInfo);
				writeItemElements(packetWriter, prodInfo);
				writeItemEnsoulOptions(packetWriter, prodInfo);
			}

			for(MultiSellIngredient i : ingredients)
			{
				int itemId = i.getItemId();
				final ItemTemplate template = itemId > 0 ? ItemHolder.getInstance().getTemplate(i.getItemId()) : null;
				ItemInfo ingredientInfo = isKeepEnchant && itemInfo != null && template != null
						&& template.getClass().equals(itemInfo.getItem().getClass()) ? itemInfo : null;

				packetWriter.writeD(itemId); //ID
				packetWriter.writeH(itemId > 0 ? template.getType2() : 0xffff);
				packetWriter.writeQ(i.getItemCount()); //Count
				packetWriter.writeH(i.getItemEnchant()); //Enchant Level
				writeItemAugment(packetWriter, ingredientInfo);
				writeItemElements(packetWriter, ingredientInfo);
				writeItemEnsoulOptions(packetWriter, ingredientInfo);
			}
		}

		return true;
	}

	//FIXME временная затычка, пока NCSoft не починят в клиенте отображение мультиселов где кол-во больше Integer.MAX_VALUE
	private static List<MultiSellIngredient> fixIngredients(List<MultiSellIngredient> ingredients)
	{
		int needFix = 0;
		for(MultiSellIngredient ingredient : ingredients)
			if(ingredient.getItemCount() > Integer.MAX_VALUE)
				needFix++;

		if(needFix == 0)
			return ingredients;

		MultiSellIngredient temp;
		List<MultiSellIngredient> result = new ArrayList<MultiSellIngredient>(ingredients.size() + needFix);
		for(MultiSellIngredient ingredient : ingredients)
		{
			ingredient = ingredient.clone();
			while(ingredient.getItemCount() > Integer.MAX_VALUE)
			{
				temp = ingredient.clone();
				temp.setItemCount(2000000000);
				result.add(temp);
				ingredient.setItemCount(ingredient.getItemCount() - 2000000000);
			}
			if(ingredient.getItemCount() > 0)
				result.add(ingredient);
		}

		return result;
	}
}