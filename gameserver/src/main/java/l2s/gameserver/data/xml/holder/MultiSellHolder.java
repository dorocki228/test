package l2s.gameserver.data.xml.holder;

import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.Config;
import l2s.gameserver.config.GveStagesConfig.GveStage;
import l2s.gameserver.model.MultiSellListContainer;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.MultiSellEntry;
import l2s.gameserver.model.base.MultiSellIngredient;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.MultiSellListPacket;
import l2s.gameserver.service.GveStageService;
import l2s.gameserver.service.MultisellDiscountService;
import l2s.gameserver.templates.item.ItemTemplate;

public class MultiSellHolder extends AbstractHolder
{
	private static final MultiSellHolder _instance;
	private final TIntObjectHashMap<MultiSellListContainer> _entries;

	public static MultiSellHolder getInstance()
	{
		return _instance;
	}

	public MultiSellListContainer getList(int id)
	{
		return _entries.get(id);
	}

	public MultiSellHolder()
	{
		_entries = new TIntObjectHashMap<>();
	}

	public void addMultiSellListContainer(int id, MultiSellListContainer list)
	{
		if(_entries.containsKey(id))
			_log.warn("MultiSell redefined: " + id);
		list.setListId(id);
		_entries.put(id, list);
	}

	public MultiSellListContainer remove(String s)
	{
		return remove(new File(s));
	}

	public MultiSellListContainer remove(File f)
	{
		return remove(Integer.parseInt(f.getName().replaceAll(".xml", "")));
	}

	public MultiSellListContainer remove(int id)
	{
		return _entries.remove(id);
	}

	private long[] parseItemIdAndCount(String s)
	{
		if(s == null || s.isEmpty())
			return null;
		String[] a = s.split(":");
		try
		{
			long id = Integer.parseInt(a[0]);
			long count = a.length > 1 ? Long.parseLong(a[1]) : 1L;
			return new long[] { id, count };
		}
		catch(Exception e)
		{
            error("", e);
			return null;
		}
	}

	public MultiSellEntry parseEntryFromStr(String s)
	{
		if(s == null || s.isEmpty())
			return null;
		String[] a = s.split("->");
		if(a.length != 2)
			return null;
		long[] ingredient;
		long[] production;
		if((ingredient = parseItemIdAndCount(a[0])) == null || (production = parseItemIdAndCount(a[1])) == null)
			return null;
		MultiSellEntry entry = new MultiSellEntry();
		entry.addIngredient(new MultiSellIngredient((int) ingredient[0], ingredient[1]));
		entry.addProduct(new MultiSellIngredient((int) production[0], production[1]));
		return entry;
	}

	public void SeparateAndSend(int listId, Player player, int npcObjectId, double taxRate) {
		for (int i : Config.ALT_DISABLED_MULTISELL) {
			if (i == listId) {
				player.sendMessage(new CustomMessage("common.Disabled"));
				return;
			}
		}

		boolean allowedByStage = GveStageService.getInstance().isMultisellAllowed(listId);
		if (!allowedByStage) {
			GveStage stage = GveStageService.getInstance().getMultisellStageInfo(listId);
			if (stage != null) {
				String message = new CustomMessage("services.gve.stages.multisell.unavailable")
					.addNumber(stage.getId())
					.addString(stage.getStartDate())
					.toString(player);
				player.sendMessage(message);
				return;
			}
		}

		MultiSellListContainer list = getList(listId);
		if (list == null) {
			player.sendMessage(new CustomMessage("common.Disabled"));
			return;
		}
		SeparateAndSend(list, player, npcObjectId, taxRate);
	}

	public void SeparateAndSend(MultiSellListContainer list, Player player, int npcObjectId, double taxRate)
	{
		list = generateMultiSell(list, player, npcObjectId, taxRate);

		MultiSellListContainer temp = new MultiSellListContainer();
        temp.setListId(list.getListId());
		temp.setType(list.getType());

		player.setMultisell(list);

        int page = 1;
        for(MultiSellEntry e : list.getEntries())
		{
			if(temp.getEntries().size() == Config.MULTISELL_SIZE)
			{
				player.sendPacket(new MultiSellListPacket(temp, page, 0));
				++page;
				temp = new MultiSellListContainer();
				temp.setListId(list.getListId());
			}
			temp.addEntry(e);
		}
		player.sendPacket(new MultiSellListPacket(temp, page, 1));
	}

	private void ingredientDiscount(MultiSellEntry entry) {
		if(MultisellDiscountService.getInstance().isDiscountDefault()) {
			return;
		}
		double discountPercent = MultisellDiscountService.getInstance().getDiscountPercent();
		for(MultiSellIngredient ingredient : entry.getIngredients()) {
			long oldCount = ingredient.getItemCount();
			ingredient.setItemCount(Math.max(1, (long) Math.ceil(oldCount * discountPercent)));
		}
	}

	private MultiSellListContainer generateMultiSell(MultiSellListContainer container, Player player, int npcObjectId, double taxRate)
	{
		MultiSellListContainer list = new MultiSellListContainer();
		list.setListId(container.getListId());
		list.setType(container.getType());

		boolean enchant = container.isKeepEnchant();
		boolean notax = container.isNoTax();
		boolean showall = container.isShowAll();
		boolean nokey = container.isNoKey();

		list.setShowAll(showall);
		list.setKeepEnchant(enchant);
		list.setNoTax(notax);
		list.setNoKey(nokey);
		list.setBBSAllowed(container.isBBSAllowed());
		list.setDisabled(container.isDisabled());
		list.setNpcObjectId(npcObjectId);
		list.setPriceMultiplier(container.getPriceMultiplier());
		list.setDiscount(container.isDiscount());

		ItemInstance[] items = player.getInventory().getItems();
		for(MultiSellEntry origEntry : container.getEntries())
		{
			MultiSellEntry ent = origEntry.clone();
			List<MultiSellIngredient> ingridients;
			if(!notax && taxRate > 0.0)
			{
                ingridients = new ArrayList<>(ent.getIngredients().size() + 1);
                long adena = 0L;
                double tax = 0.0;
                for(MultiSellIngredient i : ent.getIngredients())
					if(i.getItemId() == 57)
					{
						adena += i.getItemCount();
						tax += i.getItemCount() * taxRate;
					}
					else
					{
						ingridients.add(i);
						if(i.getItemId() == -200)
							tax += (double) (i.getItemCount() / 120L) * 1000L * taxRate * 100.0;
						if(i.getItemId() < 1)
							continue;
						ItemTemplate item = ItemHolder.getInstance().getTemplate(i.getItemId());
						if(!item.isStackable())
							continue;
						tax += item.getReferencePrice() * i.getItemCount() * taxRate;
					}
				adena = Math.round(adena + tax);
				if(adena > 0L)
					ingridients.add(new MultiSellIngredient(57, Math.round(adena * container.getPriceMultiplier())));
				ent.setTax(Math.round(tax));
				ent.getIngredients().clear();
				ent.getIngredients().addAll(ingridients);
			}
			else
				ingridients = ent.getIngredients();
			if(list.isDiscount()) {
				ingredientDiscount(ent);
			}
			if(showall)
				list.addEntry(ent);

			else
			{
				List<Integer> itms = new ArrayList<>();
				for(MultiSellIngredient ingredient : ingridients)
				{
					ItemTemplate template = ingredient.getItemId() <= 0 ? null : ItemHolder.getInstance().getTemplate(ingredient.getItemId());
					if(ingredient.getItemId() <= 0 || nokey || template.isEquipment())
					{
						if(ingredient.getItemId() == 12374)
							continue;
						if(ingredient.getItemId() == -200)
						{
							if(itms.contains(ingredient.getItemId()) || player.getClan() == null || player.getClan().getReputationScore() < ingredient.getItemCount())
								continue;
							itms.add(ingredient.getItemId());
						}
						else if(ingredient.getItemId() == -100)
						{
							if(itms.contains(ingredient.getItemId()) || player.getPcBangPoints() < ingredient.getItemCount())
								continue;
							itms.add(ingredient.getItemId());
						}
						else if(ingredient.getItemId() == -300)
						{
							if(itms.contains(ingredient.getItemId()) || player.getFame() < ingredient.getItemCount())
								continue;
							itms.add(ingredient.getItemId());
						}
						else
							for(ItemInstance item2 : items)
								if(item2.getItemId() == ingredient.getItemId())
									if(!itms.contains(enchant ? ingredient.getItemId() + ingredient.getItemEnchant() * 100000L : ingredient.getItemId()))
										if(item2.getEnchantLevel() >= ingredient.getItemEnchant())
										{
											if(item2.isStackable() && item2.getCount() < ingredient.getItemCount())
												break;
											itms.add(enchant ? ingredient.getItemId() + ingredient.getItemEnchant() * 100000 : ingredient.getItemId());
											MultiSellEntry possibleEntry = new MultiSellEntry(enchant ? ent.getEntryId() + item2.getEnchantLevel() * 100000 : ent.getEntryId());
											for(MultiSellIngredient p : ent.getProduction())
											{
												if(enchant && template.canBeEnchanted())
												{
													p.setItemEnchant(item2.getEnchantLevel());
													p.setItemAttributes(item2.getAttributes().clone());
												}
												possibleEntry.addProduct(p);
											}
											for(MultiSellIngredient ig : ingridients)
											{
												if(enchant && ig.getItemId() > 0 && ItemHolder.getInstance().getTemplate(ig.getItemId()).canBeEnchanted())
												{
													ig.setItemEnchant(item2.getEnchantLevel());
													ig.setItemAttributes(item2.getAttributes().clone());
												}
												possibleEntry.addIngredient(ig);
											}
											list.addEntry(possibleEntry);
											break;
										}
					}
				}
			}
		}
		return list;
	}

	@Override
	protected void process() {
		super.process();
		for(int id : Config.GVE_TIME_DISCOUNT.multiselsUnderDiscounts()) {
			MultiSellListContainer list = getList(id);
			if(list == null) {
				continue;
			}
			list.setDiscount(true);
		}
	}

	@Override
	public int size()
	{
		return _entries.size();
	}

	@Override
	public void clear()
	{
		_entries.clear();
	}

	static
	{
		_instance = new MultiSellHolder();
	}
}
