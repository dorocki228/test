package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.HitCondBonusHolder;
import l2s.gameserver.model.base.HitCondBonusType;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class HitCondBonusParser extends AbstractParser<HitCondBonusHolder>
{
	private static final HitCondBonusParser _instance;

	public static HitCondBonusParser getInstance()
	{
		return _instance;
	}

	private HitCondBonusParser()
	{
		super(HitCondBonusHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/pc_parameters/hit_cond_bonus.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "hit_cond_bonus.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			HitCondBonusType type = HitCondBonusType.valueOf(element.attributeValue("type"));
			double value = Double.parseDouble(element.attributeValue("value"));
			getHolder().addHitCondBonus(type, value);
		}
	}

	static
	{
		_instance = new HitCondBonusParser();
	}
}
