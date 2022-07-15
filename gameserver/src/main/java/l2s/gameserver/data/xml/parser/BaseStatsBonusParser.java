package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.BaseStatsBonusHolder;
import l2s.gameserver.model.base.BaseStats;
import l2s.gameserver.templates.BaseStatsBonus;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

/**
 * @author Bonux
**/
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
	protected void readData(Element rootElement, boolean custom) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element element = iterator.next();
			if ("STR".equalsIgnoreCase(element.getName())) {
				addBonus(element, BaseStats.STR);
			} else if ("INT".equalsIgnoreCase(element.getName())) {
				addBonus(element, BaseStats.INT);
			} else if ("CON".equalsIgnoreCase(element.getName())) {
				addBonus(element, BaseStats.CON);
			} else if ("MEN".equalsIgnoreCase(element.getName())) {
				addBonus(element, BaseStats.MEN);
			} else if ("DEX".equalsIgnoreCase(element.getName())) {
				addBonus(element, BaseStats.DEX);
			} else if ("WIT".equalsIgnoreCase(element.getName())) {
				addBonus(element, BaseStats.WIT);
			}/* else if ("CHA".equalsIgnoreCase(element.getName())) {
				addBonus(element, BaseStats.CHA);
			} else if ("LUC".equalsIgnoreCase(element.getName())) {
				addBonus(element, BaseStats.LUC);
			}*/
		}
	}

	private void addBonus(Element element, BaseStats baseStats) {
		for (Element e : element.elements()) {
			int value = Integer.parseInt(e.attributeValue("value"));
			double bonus = Double.parseDouble(e.attributeValue("bonus"));

			getHolder().addBaseStatsBonus(baseStats, value, bonus);
		}
	}
}