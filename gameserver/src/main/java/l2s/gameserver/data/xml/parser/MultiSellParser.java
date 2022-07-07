package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.model.MultiSellListContainer;
import l2s.gameserver.model.base.MultiSellEntry;
import l2s.gameserver.model.base.MultiSellIngredient;
import l2s.gameserver.templates.item.ItemTemplate;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public class MultiSellParser extends AbstractParser<MultiSellHolder>
{
	private static final MultiSellParser _instance = new MultiSellParser();

	public static MultiSellParser getInstance()
	{
		return _instance;
	}

	protected MultiSellParser()
	{
		super(MultiSellHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/multisell");
	}

    @Override
	public String getDTDFileName()
	{
		return "multisell.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		int id = Integer.parseInt(_currentFile.replace(".xml", ""));
		MultiSellListContainer list = new MultiSellListContainer();

		int entryId = 0;
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element element = iterator.next();

			if("config".equalsIgnoreCase(element.getName()))
			{
				String line = element.attributeValue("show_all");
				if (line != null)
					list.setShowAll(Boolean.parseBoolean(line));
				line = element.attributeValue("no_tax");
				if (line != null)
					list.setNoTax(Boolean.parseBoolean(line));
				line = element.attributeValue("keep_enchanted");
				if (line != null)
					list.setKeepEnchant(Boolean.parseBoolean(line));
				line = element.attributeValue("no_key");
				if (line != null)
					list.setNoKey(Boolean.parseBoolean(line));
				line = element.attributeValue("bbs_allowed");
				if (line != null)
					list.setBBSAllowed(Boolean.parseBoolean(line));
				line = element.attributeValue("disabled");
				if (line != null)
					list.setDisabled(Boolean.parseBoolean(line));
				line = element.attributeValue("price_multiplier");
				if (line != null)
					list.setPriceMultiplier(Double.parseDouble(line));
                line = element.attributeValue("type");
                if (line != null)
                    list.setType(MultiSellListContainer.MultisellType.valueOf(line));
			}
			else if("item".equalsIgnoreCase(element.getName()))
			{
				MultiSellEntry e = parseEntry(element, id);
				if(e != null)
				{
					e.setEntryId(entryId++);
					if(list.getPriceMultiplier() != 1.0D)
					{
						e.getIngredients().stream()
								.filter(ingredient -> ingredient.getItemId() == ItemTemplate.ITEM_ID_ADENA)
								.forEach(ingredient ->
										ingredient.setItemCount(Math.round(ingredient.getItemCount() * list.getPriceMultiplier())));
					}
					list.addEntry(e);
				}
			}
		}

		addMultiSellListContainer(id, list);
	}

	protected MultiSellEntry parseEntry(Element n, int multiSellId)
	{
        boolean free = Boolean.parseBoolean(n.attributeValue("free", "false"));

        MultiSellEntry entry = new MultiSellEntry(free);

		for(Iterator<Element> iterator = n.elementIterator(); iterator.hasNext();)
		{
			String line;
			Element d = iterator.next();

			if("ingredient".equalsIgnoreCase(d.getName()))
			{
				int id = Integer.parseInt(d.attributeValue("id"));
				long count = Long.parseLong(d.attributeValue("count"));
				MultiSellIngredient mi = new MultiSellIngredient(id, count);
				line = d.attributeValue("enchant");
				if(line != null)
					mi.setItemEnchant(Integer.parseInt(line));
				line = d.attributeValue("mantainIngredient");
				if(line != null)
					mi.setMantainIngredient(Boolean.parseBoolean(line));

				entry.addIngredient(mi);
			}
			else if("production".equalsIgnoreCase(d.getName()))
			{
				int id = Integer.parseInt(d.attributeValue("id"));
				long count = Long.parseLong(d.attributeValue("count"));
                int chance = d.attributeValue("chance") == null ? 0 : Integer.parseInt(d.attributeValue("chance"));

                MultiSellIngredient mi = new MultiSellIngredient(id, count, chance);
				line = d.attributeValue("enchant");
				if(line != null)
					mi.setItemEnchant(Integer.parseInt(line));
				line = d.attributeValue("activeStage");
				if (line != null) {
					mi.setActiveStage(Integer.parseInt(line));
				}

				entry.addProduct(mi);
			}
		}

		if(entry.getProduction().isEmpty() || (!entry.isFree() && entry.getIngredients().isEmpty()))
		{
			_log.warn("MultiSell [" + multiSellId + "] is empty!");
			return null;
		}

		if(!Config.ALT_SELL_ITEM_ONE_ADENA && entry.getIngredients().size() == 1 && entry.getProduction().size() == 1
				&& entry.getIngredients().get(0).getItemId() == 57)
		{
			ItemTemplate item = ItemHolder.getInstance().getTemplate(entry.getProduction().get(0).getItemId());
			if(item == null)
			{
				_log.warn("MultiSell [" + multiSellId + "] Production [" + entry.getProduction().get(0).getItemId() + "] not found!");
				return null;
			}
			if(multiSellId < 70000 || multiSellId > 70010) //FIXME hardcode. Все кроме GM Shop
				if(item.getReferencePrice() > entry.getIngredients().get(0).getItemCount())
					_log.warn("MultiSell [" + multiSellId + "] Production '" + item.getName() + "' [" + entry.getProduction().get(0).getItemId() + "] price is lower than referenced | " + item.getReferencePrice() + " > " + entry.getIngredients().get(0).getItemCount());
		}

		return entry;
	}

	protected void addMultiSellListContainer(int id, MultiSellListContainer list)
	{
		getHolder().addMultiSellListContainer(id, list);
	}
}
