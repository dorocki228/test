package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.EnsoulHolder;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.templates.item.support.Ensoul;
import l2s.gameserver.templates.item.support.EnsoulFee;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class EnsoulParser extends AbstractParser<EnsoulHolder>
{
	private static final EnsoulParser _instance = new EnsoulParser();

	public static EnsoulParser getInstance()
	{
		return _instance;
	}

	private EnsoulParser()
	{
		super(EnsoulHolder.getInstance());
	}

	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/ensoul_data.xml");
	}

	public String getDTDFileName()
	{
		return "ensoul_data.dtd";
	}

	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator("ensoul_fee_data");
		while(iterator.hasNext())
		{
			Element element = (Element) iterator.next();
			Iterator<Element> feeIterator = element.elementIterator("ensoul_fee");
			while(feeIterator.hasNext())
			{
				Element feeElement = (Element) feeIterator.next();
				ItemGrade grade = ItemGrade.valueOf(feeElement.attributeValue("grade").toUpperCase());
				EnsoulFee ensoulFee = new EnsoulFee();
				Iterator<Element> feeInfoIterator = feeElement.elementIterator("ensoul_fee_info");
				while(feeInfoIterator.hasNext())
				{
					Element feeInfoElement = (Element) feeInfoIterator.next();
					int type = Integer.parseInt(feeInfoElement.attributeValue("type"));
					Iterator<Element> feeItemsIterator = feeInfoElement.elementIterator("ensoul_fee_items");
					while(feeItemsIterator.hasNext())
					{
						Element feeItemsElement = (Element) feeItemsIterator.next();
						int id = Integer.parseInt(feeItemsElement.attributeValue("id"));
						ensoulFee.addFeeInfo(type, id, EnsoulParser.parseFeeInfo(feeItemsElement));
					}
				}
				getHolder().addEnsoulFee(grade, ensoulFee);
			}
		}
		iterator = rootElement.elementIterator("ensoul_data");
		while(iterator.hasNext())
		{
			Element element = (Element) iterator.next();
			Iterator<Element> ensoulIterator = element.elementIterator("ensoul");
			while(ensoulIterator.hasNext())
			{
				Element ensoulElement = (Element) ensoulIterator.next();
				int id = Integer.parseInt(ensoulElement.attributeValue("id"));
				int itemId = ensoulElement.attributeValue("item_id") == null ? 0 : Integer.parseInt(ensoulElement.attributeValue("item_id"));
				Ensoul ensoul = new Ensoul(id, itemId);
				Iterator<Element> skillIterator = ensoulElement.elementIterator("skill");
				while(skillIterator.hasNext())
				{
					Element skillElement = (Element) skillIterator.next();
					int skillId = Integer.parseInt(skillElement.attributeValue("id"));
					int skillLevel = Integer.parseInt(skillElement.attributeValue("level"));
					ensoul.addSkill(skillId, skillLevel);
				}
				getHolder().addEnsoul(ensoul);
			}
		}
	}

	private static EnsoulFee.EnsoulFeeInfo parseFeeInfo(Element rootElement)
	{
		EnsoulFee.EnsoulFeeInfo feeInfo = new EnsoulFee.EnsoulFeeInfo();
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element element = (Element) iterator.next();
			if(element.getName().equals("insert"))
			{
				feeInfo.setInsertFee(EnsoulParser.parseFeeItems(element));
				continue;
			}
			if(element.getName().equals("change"))
			{
				feeInfo.setChangeFee(EnsoulParser.parseFeeItems(element));
				continue;
			}
			if(!element.getName().equals("remove"))
				continue;
			feeInfo.setRemoveFee(EnsoulParser.parseFeeItems(element));
		}
		return feeInfo;
	}

	private static List<ItemData> parseFeeItems(Element rootElement)
	{
		List<ItemData> items = new ArrayList<>();
		Iterator<Element> iterator = rootElement.elementIterator("item");
		while(iterator.hasNext())
		{
			Element element = (Element) iterator.next();
			int itemId = Integer.parseInt(element.attributeValue("id"));
			long itemCount = Long.parseLong(element.attributeValue("count"));
			items.add(new ItemData(itemId, itemCount));
		}
		return items;
	}
}
