package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.commons.string.StringArrayUtils;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.AgathionHolder;
import l2s.gameserver.templates.cubic.AgathionTemplate;
import l2s.gameserver.templates.cubic.CubicTargetType;
import l2s.gameserver.templates.cubic.CubicUseUpType;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

/**
 * @author Bonux
 */
public final class AgathionParser extends AbstractParser<AgathionHolder>
{
	private static AgathionParser _instance = new AgathionParser();

	public static AgathionParser getInstance()
	{
		return _instance;
	}

	protected AgathionParser()
	{
		super(AgathionHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/agathions.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "agathions.dtd";
	}

	@Override
	protected void readData(Element rootElement, boolean custom) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element element = iterator.next();
			int npc_id = Integer.parseInt(element.attributeValue("npc_id"));
			int id = Integer.parseInt(element.attributeValue("id"));
			int duration = element.attributeValue("duration") == null ? -1 : Integer.parseInt(element.attributeValue("duration"));
			int delay = element.attributeValue("delay") == null ? 0 : Integer.parseInt(element.attributeValue("delay"));
			int max_count = element.attributeValue("max_count") == null ? Integer.MAX_VALUE : Integer.parseInt(element.attributeValue("max_count"));
			CubicUseUpType use_up = element.attributeValue("use_up") == null ? CubicUseUpType.INCREASE_DELAY : CubicUseUpType.valueOf(element.attributeValue("use_up").toUpperCase());
			double power = element.attributeValue("power") == null ? 0. : Double.parseDouble(element.attributeValue("power"));
			CubicTargetType target_type = element.attributeValue("target_type") == null ? CubicTargetType.BY_SKILL : CubicTargetType.valueOf(element.attributeValue("target_type").toUpperCase());
			int[] item_ids = StringArrayUtils.stringToIntArray(element.attributeValue("item_ids"), ";");
			int energy = element.attributeValue("energy") == null ? 0 : Integer.parseInt(element.attributeValue("energy"));
			int max_energy = element.attributeValue("max_energy") == null ? 0 : Integer.parseInt(element.attributeValue("max_energy"));

			AgathionTemplate template = new AgathionTemplate(npc_id, id, duration, delay, max_count, use_up, power, target_type, item_ids, energy, max_energy);

			CubicParser.parseSkills(this, template, element);

			getHolder().addAgathionTemplate(template);
		}
	}
}
