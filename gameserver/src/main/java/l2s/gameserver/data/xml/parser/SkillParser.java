package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractJDomParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.handler.skillconditions.SkillCondition;
import l2s.gameserver.handler.skillconditions.SkillConditionHandler;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.skill.SkillConditionScope;
import l2s.gameserver.skills.EffectTargetType;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.templates.skill.restoration.RestorationInfo;
import org.jdom2.Element;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.function.Function;

/**
 * @author Bonux
**/
public final class SkillParser extends AbstractJDomParser<SkillHolder>
{
	private static final SkillParser _instance = new SkillParser();

	private final IntObjectMap<IntObjectMap<StatsSet>> _skillsTables = new TreeIntObjectMap<IntObjectMap<StatsSet>>();

	public static SkillParser getInstance()
	{
		return _instance;
	}

	protected SkillParser()
	{
		super(SkillHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/skills/");
	}

	@Override
	public File getCustomXMLPath()
	{
		return Config.CUSTOM_PATH.resolve("skills").toFile();
	}

	@Override
	public String getDTDFileName()
	{
		return "skills.dtd";
	}

	@Override
	protected void onParsed()
	{
		_skillsTables.clear();

		getHolder().callInit();
	}

	@Override
	protected void readData(Element rootElement, boolean custom) throws Exception
	{
		for (Element skillElement : rootElement.getChildren()) {
			final int skillId = Integer.parseInt(skillElement.getAttributeValue("id"));
			int level = Integer.parseInt(skillElement.getAttributeValue("level"));

			_skillsTables.remove(skillId); // При парсе кастом папки, возникают проблемы, поэтому удаляем заранее.

			final StatsSet set = new StatsSet();

			set.set("skill_id", skillId);
			set.set("name", skillElement.getAttributeValue("name"));

			final IntObjectMap<RestorationInfo> restorations = new TreeIntObjectMap<RestorationInfo>();

			for (Element subElement : skillElement.getChildren()) {
				final String subName = subElement.getName();
				if (subName.equalsIgnoreCase("set"))
					set.set(subElement.getAttributeValue("name"), subElement.getAttributeValue("value"));
			}

			final StatsSet currentSet = set.clone();
			for (Entry<String, Object> entry : currentSet.entrySet())
				currentSet.set(entry.getKey(), String.valueOf(entry.getValue()));

			currentSet.set("level", level);

			currentSet.set("restoration", restorations.get(level));

			Skill skill = new Skill(currentSet);

			for (Element subElement : skillElement.getChildren()) {
				final String subName = subElement.getName();
				if (subName.equalsIgnoreCase("for"))
					parseFor(subElement, skill);
				else
				{
					final SkillConditionScope skillConditionScope = SkillConditionScope.Companion.findByXmlNodeName(subName);
					if (skillConditionScope != null) {
						for (Element condElement : subElement.getChildren()) {
							final String conditionName = condElement.getAttributeValue("name");

							final StatsSet condSet = new StatsSet();
							for (Element defElement : condElement.getChildren()) {
								final String defElementName = defElement.getName();
								if (defElementName.equalsIgnoreCase("def"))
									condSet.set(defElement.getAttributeValue("name"), parseString(defElement, "value"));
							}

							if (conditionName != null) {
								final Function<StatsSet, SkillCondition> conditionFunction = SkillConditionHandler.getInstance().getHandlerFactory(conditionName);
								if (conditionFunction != null) {
									skill.addCondition(skillConditionScope, conditionFunction.apply(condSet));
								} else {
									getLogger().atWarning().log("Missing condition for Skill Id[%d] Level[%d] Effect Scope[%s] Effect Name[%s]", skillId, level, skillConditionScope, conditionName);
								}
							}
						}
					}
				}
			}

			getHolder().addSkill(skill);
		}
	}

	protected void parseFor(Element forElement, StatTemplate template) throws Exception {
		if(!(template instanceof Skill))
			return;

		Skill skill = (Skill) template;
		for (Element element : forElement.getChildren()) {
			final String elementName = element.getName();
			if (elementName.equalsIgnoreCase("start_effect"))
				attachEffect(element, skill, EffectUseType.START, EffectTargetType.NORMAL);
			else if (elementName.equalsIgnoreCase("tick_effect"))
				attachEffect(element, skill, EffectUseType.TICK, EffectTargetType.NORMAL);
			else if (elementName.equalsIgnoreCase("self_effect"))
				attachEffect(element, skill, EffectUseType.SELF, EffectTargetType.NORMAL);
			else if (elementName.equalsIgnoreCase("effect"))
				attachEffect(element, skill, EffectUseType.NORMAL, EffectTargetType.NORMAL);
			else if (elementName.equalsIgnoreCase("pvp_effect"))
				attachEffect(element, skill, EffectUseType.NORMAL, EffectTargetType.PVP);
			else if (elementName.equalsIgnoreCase("pve_effect"))
				attachEffect(element, skill, EffectUseType.NORMAL, EffectTargetType.PVE);
			else if (elementName.equalsIgnoreCase("end_effect"))
				attachEffect(element, skill, EffectUseType.END, EffectTargetType.NORMAL);
		}
	}

	private void attachEffect(Element element, Skill skill, EffectUseType useType, EffectTargetType targetType) throws Exception {
		final StatsSet set = new StatsSet();

		if(element.getAttributeValue("chance") != null)
		{
			int chance = parseInt(element, "chance");
			if(chance <= 0)
				return;

			set.set("chance", chance);
		}

		if(element.getAttributeValue("name") != null)
			set.set("name", parseString(element, "name"));

		EffectTemplate effectTemplate = new EffectTemplate(skill, set, useType, targetType);

		parseFor(element, effectTemplate);

		for (Element subElement : element.getChildren()) {
			final String subElementName = subElement.getName();
			if (subElementName.equalsIgnoreCase("def"))
				set.set(subElement.getAttributeValue("name"), parseString(subElement, "value"));
		}

		skill.attachEffect(effectTemplate);
	}

	private void parseTable(Element element, int skillId, int firstLevel, int lastLevel)
	{
		String name = element.getAttributeValue("name");
		if(name.charAt(0) != '#')
		{
			warn("Error while parse table[" + name + "] value for skill ID[" + skillId + "] (Table name must start with #)!");
			return;
		}

		if(name.lastIndexOf('#') != 0)
		{
			warn("Error while parse table[" + name + "] value for skill ID[" + skillId + "] (Table name should not contain # character, but only start with #)!");
			return;
		}

		if(name.contains(";") || name.contains(":") || name.contains(" ") || name.contains("-"))
		{
			warn("Error while parse table[" + name + "] value for skill ID[" + skillId + "] (Table name should not contain characters: ';' ':' '-' or space)!");
			return;
		}

		StringTokenizer data = new StringTokenizer(element.getText());
		List<String> values = new ArrayList<String>();
		while(data.hasMoreTokens())
			values.add(data.nextToken());


		IntObjectMap<StatsSet> tables = _skillsTables.get(skillId);
		if(tables == null)
		{
			tables = new TreeIntObjectMap<StatsSet>();
			_skillsTables.put(skillId, tables);
		}

		int i = 0;
		for(int lvl = firstLevel; lvl <= lastLevel; lvl++)
		{
			StatsSet set = tables.get(lvl);
			if(set == null)
			{
				set = new StatsSet();
				tables.put(lvl, set);
			}
			else if(set.containsKey(name))
			{
				warn("Error while parse table[" + name + "] value for skill ID[" + skillId + "] (Skill have tables with the same name)!");
				return;
			}

			set.set(name, values.get(Math.min(i, values.size() - 1)));
			i++;
		}
	}
}
