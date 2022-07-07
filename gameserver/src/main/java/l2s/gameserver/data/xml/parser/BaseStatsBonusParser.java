package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.BaseStatsBonusHolder;
import l2s.gameserver.templates.BaseStatsBonus;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class BaseStatsBonusParser extends AbstractParser<BaseStatsBonusHolder>
{
	private static final BaseStatsBonusParser _instance = new BaseStatsBonusParser();

	public static BaseStatsBonusParser getInstance()
	{
		return _instance;
	}

	private BaseStatsBonusParser()
	{
		super(BaseStatsBonusHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/pc_parameters/base_stats_bonus_data.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "base_stats_bonus_data.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			if("base_stats_bonus".equalsIgnoreCase(element.getName()))
				for(Element e : element.elements())
				{
					int value = Integer.parseInt(e.attributeValue("value"));
					double str = Double.parseDouble(e.attributeValue("str"));
					double _int = Double.parseDouble(e.attributeValue("int"));
					double dex = Double.parseDouble(e.attributeValue("dex"));
					double wit = Double.parseDouble(e.attributeValue("wit"));
					double con = Double.parseDouble(e.attributeValue("con"));
					double men = Double.parseDouble(e.attributeValue("men"));
					getHolder().addBaseStatsBonus(value, new BaseStatsBonus(_int, str, con, men, dex, wit));
				}
		}
	}
}
