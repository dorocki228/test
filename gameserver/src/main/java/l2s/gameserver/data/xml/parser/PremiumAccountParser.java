package l2s.gameserver.data.xml.parser;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.PremiumAccountHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.templates.premiumaccount.PremiumAccountBonus;
import l2s.gameserver.templates.premiumaccount.PremiumAccountModifiers;
import l2s.gameserver.templates.premiumaccount.PremiumAccountRates;
import l2s.gameserver.templates.premiumaccount.PremiumAccountTemplate;
import l2s.gameserver.utils.Language;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class PremiumAccountParser extends StatParser<PremiumAccountHolder>
{
	private static final PremiumAccountParser _instance = new PremiumAccountParser();

	public static PremiumAccountParser getInstance()
	{
		return _instance;
	}

	private PremiumAccountParser()
	{
		super(PremiumAccountHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/premium_accounts.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "premium_accounts.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator("config");
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			Config.PREMIUM_ACCOUNT_ENABLED = Boolean.parseBoolean(element.attributeValue("enabled"));
			Config.PREMIUM_ACCOUNT_BASED_ON_GAMESERVER = Boolean.parseBoolean(element.attributeValue("based_on_gameserver"));
			Config.FREE_PA_TYPE = element.attributeValue("free_type") == null ? 0 : Integer.parseInt(element.attributeValue("free_type"));
			Config.FREE_PA_DELAY = element.attributeValue("free_delay") == null ? 0 : Integer.parseInt(element.attributeValue("free_delay"));
			Config.ENABLE_FREE_PA_NOTIFICATION = element.attributeValue("notify_free") != null && Boolean.parseBoolean(element.attributeValue("notify_free"));
		}

		if(!Config.PREMIUM_ACCOUNT_ENABLED)
			return;

		iterator = rootElement.elementIterator("account");
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			int type = Integer.parseInt(element.attributeValue("type"));
			PremiumAccountTemplate template = new PremiumAccountTemplate(type);
			Iterator<Element> subIterator = element.elementIterator();
			while(subIterator.hasNext())
			{
				Element subElement = subIterator.next();
				if("name".equalsIgnoreCase(subElement.getName()))
				{
					for(Element e : subElement.elements())
						for(Language lang : Language.VALUES)
							if(lang.getShortName().equalsIgnoreCase(e.getName()))
								template.addName(lang, e.getText());
				}
				else if("rates".equalsIgnoreCase(subElement.getName()))
				{
					double expRate = Double.parseDouble(subElement.attributeValue("exp"));
					double spRate = Double.parseDouble(subElement.attributeValue("sp"));
					double adenaRate = Double.parseDouble(subElement.attributeValue("adena"));
					double dropRate = Double.parseDouble(subElement.attributeValue("drop"));
					double spoilRate = Double.parseDouble(subElement.attributeValue("spoil"));
					double questDropRate = Double.parseDouble(subElement.attributeValue("quest_drop"));
					double questRewardRate = Double.parseDouble(subElement.attributeValue("quest_reward"));
					double confrontationPoints = Double.parseDouble(subElement.attributeValue("confrontation_points"));
					double expLossRate = Double.parseDouble(subElement.attributeValue("exp_loss"));
					double taxTpRate = Double.parseDouble(subElement.attributeValue("tax_tp"));

					template.setRates(new PremiumAccountRates(expRate, spRate, adenaRate, dropRate, spoilRate,
							questDropRate, questRewardRate, confrontationPoints,
							expLossRate, taxTpRate));
				}
				else if("modifiers".equalsIgnoreCase(subElement.getName()))
				{
					double dropChanceMod = Double.parseDouble(subElement.attributeValue("drop_chance"));
					double spoilChanceMod = Double.parseDouble(subElement.attributeValue("spoil_chance"));
					double enchant = Double.parseDouble(subElement.attributeValue("enchant"));

					template.setModifiers(new PremiumAccountModifiers(dropChanceMod, spoilChanceMod, enchant));
				}
				else if("bonus".equalsIgnoreCase(subElement.getName()))
				{
					double enchantChanceBonus = Double.parseDouble(subElement.attributeValue("enchant_chance"));
					template.setBonus(new PremiumAccountBonus(enchantChanceBonus));
				}
				else if("give_items_on_start".equalsIgnoreCase(subElement.getName()))
					for(Element e : subElement.elements())
					{
						int itemId = Integer.parseInt(e.attributeValue("id"));
						long itemCount = Long.parseLong(e.attributeValue("count"));
						template.addGiveItemOnStart(new ItemData(itemId, itemCount));
					}
				else if("take_items_on_end".equalsIgnoreCase(subElement.getName()))
					for(Element e : subElement.elements())
					{
						int itemId = Integer.parseInt(e.attributeValue("id"));
						long itemCount = Long.parseLong(e.attributeValue("count"));
						template.addTakeItemOnEnd(new ItemData(itemId, itemCount));
					}
				else if("fee".equalsIgnoreCase(subElement.getName()))
				{
					int delay = subElement.attributeValue("delay") == null ? -1 : Integer.parseInt(subElement.attributeValue("delay"));
					for(Element e2 : subElement.elements())
					{
						int itemId2 = Integer.parseInt(e2.attributeValue("id"));
						long itemCount2 = Long.parseLong(e2.attributeValue("count"));
						template.addFee(delay, new ItemData(itemId2, itemCount2));
					}
				}
				else if("stats".equalsIgnoreCase(subElement.getName()))
					parseFor(subElement, template);
				else if("triggers".equalsIgnoreCase(subElement.getName()))
					parseTriggers(subElement, template);
				else
				{
					if(!"skills".equalsIgnoreCase(subElement.getName()))
						continue;
					Iterator<Element> nextIterator = subElement.elementIterator("skill");
					while(nextIterator.hasNext())
					{
						Element nextElement = nextIterator.next();
						int id = Integer.parseInt(nextElement.attributeValue("id"));
						int level = Integer.parseInt(nextElement.attributeValue("level"));
						template.attachSkill(SkillHolder.getInstance().getSkillEntry(id, level));
					}
				}
			}
			getHolder().addPremiumAccount(template);
		}
	}

	@Override
	protected void afterParseActions()
	{
		if(getHolder().size() == 0)
			Config.PREMIUM_ACCOUNT_ENABLED = false;
	}

	@Override
	protected Object getTableValue(String name, int... arg)
	{
		return null;
	}

}
