package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.commons.string.StringArrayUtils;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.EnchantItemHolder;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.support.EnchantScroll;
import l2s.gameserver.templates.item.support.EnchantType;
import l2s.gameserver.templates.item.support.EnchantVariation;
import l2s.gameserver.templates.item.support.FailResultType;
import org.dom4j.Element;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class EnchantItemParser extends AbstractParser<EnchantItemHolder>
{
	private static final EnchantItemParser _instance;

	public static EnchantItemParser getInstance()
	{
		return _instance;
	}

	private EnchantItemParser()
	{
		super(EnchantItemHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/enchant_items.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "enchant_items.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		int defaultMaxEnchant = 0;
		boolean defaultFailEffect = false;
		Element defaultElement = rootElement.element("default");
		if(defaultElement != null)
		{
			defaultMaxEnchant = Integer.parseInt(defaultElement.attributeValue("max_enchant"));
			defaultFailEffect = Boolean.parseBoolean(defaultElement.attributeValue("show_fail_effect"));
		}
		Iterator<Element> iterator1 = rootElement.elementIterator("chance_variations");
		while(iterator1.hasNext())
		{
			Element element1 = iterator1.next();
			Iterator<Element> iterator2 = element1.elementIterator("variation");
			while(iterator2.hasNext())
			{
				Element element2 = iterator2.next();
				EnchantVariation variation = new EnchantVariation(Integer.parseInt(element2.attributeValue("id")));
				Iterator<Element> iterator3 = element2.elementIterator("enchant");
				while(iterator3.hasNext())
				{
					Element element3 = iterator3.next();
					int[] enchantLvl = StringArrayUtils.stringToIntArray(element3.attributeValue("level"), "-");
					double baseChance = Double.parseDouble(element3.attributeValue("base_chance"));
					double magicWeaponChance = element3.attributeValue("magic_weapon_chance") == null ? baseChance : Double.parseDouble(element3.attributeValue("magic_weapon_chance"));
					double fullBodyChance = element3.attributeValue("full_body_armor_chance") == null ? baseChance : Double.parseDouble(element3.attributeValue("full_body_armor_chance"));
					boolean succVisualEffect = element3.attributeValue("success_visual_effect") != null && Boolean.parseBoolean(element3.attributeValue("success_visual_effect"));
					if(enchantLvl.length == 2)
						for(int i = enchantLvl[0]; i <= enchantLvl[1]; ++i)
							variation.addLevel(new EnchantVariation.EnchantLevel(i, baseChance, magicWeaponChance, fullBodyChance, succVisualEffect));
					else
						variation.addLevel(new EnchantVariation.EnchantLevel(enchantLvl[0], baseChance, magicWeaponChance, fullBodyChance, succVisualEffect));
				}
				getHolder().addEnchantVariation(variation);
			}
		}
		Iterator<Element> iterator4 = rootElement.elementIterator("enchant_scroll");
		while(iterator4.hasNext())
		{
			Element enchantItemElement = iterator4.next();
			int itemId = Integer.parseInt(enchantItemElement.attributeValue("id"));
			int variation2 = Integer.parseInt(enchantItemElement.attributeValue("variation"));
			int maxEnchant = enchantItemElement.attributeValue("max_enchant") == null ? defaultMaxEnchant : Integer.parseInt(enchantItemElement.attributeValue("max_enchant"));
			FailResultType resultType = FailResultType.valueOf(enchantItemElement.attributeValue("on_fail"));
			int enchantDropCount = enchantItemElement.attributeValue("enchant_drop_count") == null ? Integer.MAX_VALUE : Integer.parseInt(enchantItemElement.attributeValue("enchant_drop_count"));
			EnchantType enchantType = enchantItemElement.attributeValue("type") == null ? EnchantType.ALL : EnchantType.valueOf(enchantItemElement.attributeValue("type"));
            String[] array = null;
			if(enchantItemElement.attributeValue("grade") == null)
				array = new String[] { "NONE" };
			else
				array = enchantItemElement.attributeValue("grade").split(";");
			String[] grades = array;
            Set<ItemGrade> gradesSet = new HashSet<>();
            for(String grade : grades)
				gradesSet.add(ItemGrade.valueOf(grade.toUpperCase()));
			boolean failEffect = enchantItemElement.attributeValue("show_fail_effect") == null ? defaultFailEffect : Boolean.parseBoolean(enchantItemElement.attributeValue("show_fail_effect"));
			int minEnchantStep = enchantItemElement.attributeValue("min_enchant_step") == null ? 1 : Integer.parseInt(enchantItemElement.attributeValue("min_enchant_step"));
			int maxEnchantStep = enchantItemElement.attributeValue("max_enchant_step") == null ? 1 : Integer.parseInt(enchantItemElement.attributeValue("max_enchant_step"));
			EnchantScroll item = new EnchantScroll(itemId, variation2, maxEnchant, enchantType, gradesSet, resultType, enchantDropCount, failEffect, minEnchantStep, maxEnchantStep);

			Iterator<Element> iterator5 = enchantItemElement.elementIterator();
			while(iterator5.hasNext())
			{
				Element element4 = iterator5.next();
				if("item_list".equals(element4.getName()))
					for(Element e : element4.elements())
						item.addItemId(Integer.parseInt(e.attributeValue("id")));
			}
			getHolder().addEnchantScroll(item);
		}
	}

	static
	{
		_instance = new EnchantItemParser();
	}
}
