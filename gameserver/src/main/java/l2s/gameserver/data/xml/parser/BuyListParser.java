package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.BuyListHolder;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.npc.BuyListTemplate;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class BuyListParser extends AbstractParser<BuyListHolder>
{
	private static final BuyListParser _instance;

	public static BuyListParser getInstance()
	{
		return _instance;
	}

	private BuyListParser()
	{
		super(BuyListHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/buylists/");
	}

	@Override
	public String getDTDFileName()
	{
		return "buylist.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator("npc");
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			int npcId = Integer.parseInt(element.attributeValue("id"));
			Iterator<Element> buylistIterator = element.elementIterator("buylist");
			while(buylistIterator.hasNext())
			{
				Element buylistElement = buylistIterator.next();
				int buylistId = Integer.parseInt(buylistElement.attributeValue("id"));
				int baseMarkup = buylistElement.attributeValue("base_markup") == null ? 0 : Integer.parseInt(buylistElement.attributeValue("base_markup"));
				BuyListTemplate buyList = new BuyListTemplate(npcId, buylistId, baseMarkup);
				Iterator<Element> itemIterator = buylistElement.elementIterator("item");
				while(itemIterator.hasNext())
				{
					Element itemElement = itemIterator.next();
					int itemId = Integer.parseInt(itemElement.attributeValue("id"));
					ItemTemplate template = ItemHolder.getInstance().getTemplate(itemId);
					if(template == null)
						_log.warn("Template not found for item ID: " + itemId + " for npc ID: " + npcId + " for buylist ID: " + buylistId);
					else
					{
						if(!checkItem(template))
							continue;
						double itemMarkup = (itemElement.attributeValue("markup") == null ? baseMarkup : Integer.parseInt(itemElement.attributeValue("markup"))) / 100.0 + 1.0;
						long itemPrice = npcId > 0 ? itemElement.attributeValue("price") == null ? Math.round(template.getReferencePrice() * itemMarkup) : Long.parseLong(itemElement.attributeValue("price")) : 0L;
						long itemCount = itemElement.attributeValue("count") == null ? 0L : Long.parseLong(itemElement.attributeValue("count"));
						int itemRechargeTime = itemElement.attributeValue("time") == null ? 0 : Integer.parseInt(itemElement.attributeValue("time"));
						TradeItem item = new TradeItem();
						item.setItemId(itemId);
						item.setOwnersPrice(itemPrice);
						item.setCount(itemCount);
						item.setCurrentValue(itemCount);
						item.setLastRechargeTime((int) (System.currentTimeMillis() / 60000L));
						item.setRechargeTime(itemRechargeTime);
						buyList.addItem(item);
					}
				}
				getHolder().addBuyList(buyList);
			}
		}
	}

	private static boolean checkItem(ItemTemplate template)
	{
		if(template.isEquipment() && !template.isForPet() && Config.ALT_SHOP_PRICE_LIMITS.length > 0)
		{
			int i = 0;
			while(i < Config.ALT_SHOP_PRICE_LIMITS.length)
				if(template.getBodyPart() == Config.ALT_SHOP_PRICE_LIMITS[i])
				{
					if(template.getReferencePrice() > Config.ALT_SHOP_PRICE_LIMITS[i + 1])
						return false;
					break;
				}
				else
					i += 2;
		}
		if(Config.ALT_SHOP_UNALLOWED_ITEMS.length > 0)
			for(int j : Config.ALT_SHOP_UNALLOWED_ITEMS)
				if(template.getItemId() == j)
					return false;
		return true;
	}

	static
	{
		_instance = new BuyListParser();
	}
}
