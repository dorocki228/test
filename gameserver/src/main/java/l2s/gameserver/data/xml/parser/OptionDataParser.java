package l2s.gameserver.data.xml.parser;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.OptionDataHolder;
import l2s.gameserver.model.Skill;
import l2s.gameserver.skills.EffectTargetType;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.templates.OptionDataTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.skill.EffectTemplate;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

/**
 * @author VISTALL
 * @date 20:36/19.05.2011
 */
public final class OptionDataParser extends StatParser<OptionDataHolder>
{
	private static final OptionDataParser _instance = new OptionDataParser();

	public static OptionDataParser getInstance()
	{
		return _instance;
	}

	protected OptionDataParser()
	{
		super(OptionDataHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/option_data");
	}

	@Override
	public String getDTDFileName()
	{
		return "option_data.dtd";
	}

	@Override
	protected void readData(Element rootElement, boolean custom) throws Exception
	{
		for(Iterator<Element> itemIterator = rootElement.elementIterator(); itemIterator.hasNext();)
		{
			Element optionDataElement = itemIterator.next();
			OptionDataTemplate template = new OptionDataTemplate(Integer.parseInt(optionDataElement.attributeValue("id")));
			for(Iterator<Element> subIterator = optionDataElement.elementIterator(); subIterator.hasNext();)
			{
				Element subElement = subIterator.next();
				String subName = subElement.getName();
				if(subName.equalsIgnoreCase("for"))
					parseFor(subElement, template);
				else if(subName.equalsIgnoreCase("triggers"))
					parseTriggers(subElement, template);
				else if(subName.equalsIgnoreCase("skills"))
				{
					for(Iterator<Element> nextIterator = subElement.elementIterator(); nextIterator.hasNext();)
					{
						Element nextElement = nextIterator.next();
						int id = Integer.parseInt(nextElement.attributeValue("id"));
						int level = Integer.parseInt(nextElement.attributeValue("level"));

						SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, id, level);

						if(skillEntry != null)
							template.addSkill(skillEntry);
						else
							warn("Skill not found(" + id + "," + level + ") for option data:" + template.getId() + "; file:" + getCurrentFileName());
					}
				}
			}
			getHolder().addTemplate(template);
		}
	}

	private void parseFor(Element forElement, OptionDataTemplate template) throws Exception {
		for(Iterator<Element> nextIterator = forElement.elementIterator(); nextIterator.hasNext();)
		{
			Element element = nextIterator.next();
			final String elementName = element.getName();
			if (elementName.equalsIgnoreCase("start_effect"))
				attachEffect(element, template, EffectUseType.START, EffectTargetType.NORMAL);
			else if (elementName.equalsIgnoreCase("tick_effect"))
				attachEffect(element, template, EffectUseType.TICK, EffectTargetType.NORMAL);
			else if (elementName.equalsIgnoreCase("self_effect"))
				attachEffect(element, template, EffectUseType.SELF, EffectTargetType.NORMAL);
			else if (elementName.equalsIgnoreCase("effect"))
				attachEffect(element, template, EffectUseType.NORMAL, EffectTargetType.NORMAL);
			else if (elementName.equalsIgnoreCase("pvp_effect"))
				attachEffect(element, template, EffectUseType.NORMAL, EffectTargetType.PVP);
			else if (elementName.equalsIgnoreCase("pve_effect"))
				attachEffect(element, template, EffectUseType.NORMAL, EffectTargetType.PVE);
			else if (elementName.equalsIgnoreCase("end_effect"))
				attachEffect(element, template, EffectUseType.END, EffectTargetType.NORMAL);
		}
	}

	private void attachEffect(Element element, OptionDataTemplate template, EffectUseType useType, EffectTargetType targetType) throws Exception {
		final StatsSet set = new StatsSet();

		if(element.attributeValue("name") != null)
			set.set("name", parseString(element, "name"));

		EffectTemplate effectTemplate = new EffectTemplate(null, set, useType, targetType);

		for(Iterator<Element> nextIterator = element.elementIterator(); nextIterator.hasNext();)
		{
			Element subElement = nextIterator.next();
			final String subElementName = subElement.getName();
			if (subElementName.equalsIgnoreCase("def"))
				set.set(subElement.attributeValue("name"), parseString(subElement, "value"));
		}

		template.addEffect(effectTemplate.getHandler());
	}

	@Override
	protected Object getTableValue(String name, int... arg)
	{
		return null;
	}
}
