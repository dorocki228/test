package l2s.gameserver.config.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.config.xml.holder.GveRewardHolder;
import l2s.gameserver.templates.gve.SimpleReward;
import l2s.gameserver.templates.item.ItemGrade;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class GveRewardParser extends AbstractParser<GveRewardHolder>
{
	private static final GveRewardParser _instance = new GveRewardParser();

	public static GveRewardParser getInstance()
	{
		return _instance;
	}

	protected GveRewardParser()
	{
		super(GveRewardHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File("config/gve_rewards.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "gve_rewards.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			if("item".equals(element.getName()))
			{
				int id = Integer.parseInt(element.attributeValue("id"));

				int exp = Integer.parseInt(element.attributeValue("exp"));
				int sp = Integer.parseInt(element.attributeValue("sp"));
				int adena = Integer.parseInt(element.attributeValue("adena"));

				getHolder().addItemReward(id, new SimpleReward(exp, sp, adena));
			}
			else if("level".equals(element.getName()))
			{
				int lvl = Integer.parseInt(element.attributeValue("lvl"));

				int exp = Integer.parseInt(element.attributeValue("exp"));
				int sp = Integer.parseInt(element.attributeValue("sp"));
				int adena = Integer.parseInt(element.attributeValue("adena"));

				getHolder().addLevelReward(lvl, new SimpleReward(exp, sp, adena));
			}
			else if("pvp".equals(element.getName()))
			{
				int count = Integer.parseInt(element.attributeValue("count"));

				int exp = Integer.parseInt(element.attributeValue("exp"));
				int sp = Integer.parseInt(element.attributeValue("sp"));
				int adena = Integer.parseInt(element.attributeValue("adena"));

				getHolder().addPvpReward(count, new SimpleReward(exp, sp, adena));
			}
			else if("set".equals(element.getName()))
			{
				int id = Integer.parseInt(element.attributeValue("chest"));

				int exp = Integer.parseInt(element.attributeValue("exp"));
				int sp = Integer.parseInt(element.attributeValue("sp"));
				int adena = Integer.parseInt(element.attributeValue("adena"));

				getHolder().addSetReward(id, new SimpleReward(exp, sp, adena));
			}
			else if("enchant".equals(element.getName()))
			{
				String[] slotArr = element.attributeValue("slot").split(";");
				ItemGrade grade = ItemGrade.valueOf(element.attributeValue("grade"));
				int value = Integer.parseInt(element.attributeValue("value"));
				int exp = Integer.parseInt(element.attributeValue("exp"));
				int sp = Integer.parseInt(element.attributeValue("sp"));
				int adena = Integer.parseInt(element.attributeValue("adena"));

				for(String slot : slotArr)
					getHolder().addEnchantReward(Integer.valueOf(slot), grade, value, new SimpleReward(exp, sp, adena));
			}
			else if("noble".equals(element.getName()))
			{
				int exp = Integer.parseInt(element.attributeValue("exp"));
				int sp = Integer.parseInt(element.attributeValue("sp"));
				int adena = Integer.parseInt(element.attributeValue("adena"));

				getHolder().addNobleReward(new SimpleReward(exp, sp, adena));
			}
			else if("hero".equals(element.getName()))
			{
				int exp = Integer.parseInt(element.attributeValue("exp"));
				int sp = Integer.parseInt(element.attributeValue("sp"));
				int adena = Integer.parseInt(element.attributeValue("adena"));

				getHolder().addHeroReward(new SimpleReward(exp, sp, adena));
			}
			else if("title_color".equals(element.getName()))
			{
				int reward = Integer.parseInt(element.attributeValue("reward"));
				int in = Integer.decode("0x" + element.attributeValue("color"));
				int red = (in >> 16) & 0xFF;
				int green = (in >> 8) & 0xFF;
				int blue = (in >> 0) & 0xFF;
				int out = (blue << 16) | (green << 8) | (red << 0);
				getHolder().addTitleColorReward(reward, out);
			}

		}
	}
}
