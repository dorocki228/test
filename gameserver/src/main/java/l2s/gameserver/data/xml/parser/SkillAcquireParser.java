package l2s.gameserver.data.xml.parser;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.stats.conditions.Condition;
import org.dom4j.Element;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class SkillAcquireParser extends StatParser<SkillAcquireHolder>
{
	private static final SkillAcquireParser _instance;

	public static SkillAcquireParser getInstance()
	{
		return _instance;
	}

	protected SkillAcquireParser()
	{
		super(SkillAcquireHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/skill_tree/");
	}

	@Override
	public String getDTDFileName()
	{
		return "tree.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator("sub_unit_skill_tree");
		while(iterator.hasNext())
			getHolder().addAllSubUnitLearns(parseSkillLearn(iterator.next()));
		iterator = rootElement.elementIterator("pledge_skill_tree");
		while(iterator.hasNext())
			getHolder().addAllPledgeLearns(parseSkillLearn(iterator.next()));
		iterator = rootElement.elementIterator("hero_skill_tree");
		while(iterator.hasNext())
			getHolder().addAllHeroLearns(parseSkillLearn(iterator.next()));
		iterator = rootElement.elementIterator("fishing_skill_tree");
		while(iterator.hasNext())
		{
			Element nxt = iterator.next();
			Iterator<Element> classIterator = nxt.elementIterator("race");
			while(classIterator.hasNext())
			{
				Element classElement = classIterator.next();
				int race = Integer.parseInt(classElement.attributeValue("id"));
				Set<SkillLearn> learns = parseSkillLearn(classElement);
				getHolder().addAllFishingLearns(race, learns);
			}
		}
		iterator = rootElement.elementIterator("gm_skill_tree");
		while(iterator.hasNext())
			getHolder().addAllGMLearns(parseSkillLearn(iterator.next()));
		iterator = rootElement.elementIterator("normal_skill_tree");
		while(iterator.hasNext())
		{
			Element nxt = iterator.next();
			Iterator<Element> classIterator = nxt.elementIterator("class");
			while(classIterator.hasNext())
			{
				Element classElement = classIterator.next();
				if(classElement.attributeValue("id") != null)
				{
					int classId = Integer.parseInt(classElement.attributeValue("id"));
					Set<SkillLearn> learns = parseSkillLearn(classElement);
					getHolder().addAllNormalSkillLearns(classId, learns);
				}
				if(classElement.attributeValue("level") != null)
				{
					ClassLevel classLevel = ClassLevel.valueOf(classElement.attributeValue("level").toUpperCase());
					Set<SkillLearn> learns = parseSkillLearn(classElement);
					for(ClassId classId2 : ClassId.VALUES)
						if(classId2.isOfLevel(classLevel))
							getHolder().addAllNormalSkillLearns(classId2.getId(), learns);
				}
			}
		}
		iterator = rootElement.elementIterator("general_skill_tree");
		while(iterator.hasNext())
		{
			Element nxt = iterator.next();
			getHolder().addAllGeneralSkillLearns(-1, parseSkillLearn(nxt));
			Iterator<Element> classIterator = nxt.elementIterator("class");
			while(classIterator.hasNext())
			{
				Element classElement = classIterator.next();
				if(classElement.attributeValue("id") != null)
				{
					int classId = Integer.parseInt(classElement.attributeValue("id"));
					Set<SkillLearn> learns = parseSkillLearn(classElement);
					getHolder().addAllGeneralSkillLearns(classId, learns);
				}
				if(classElement.attributeValue("level") != null)
				{
					ClassLevel classLevel = ClassLevel.valueOf(classElement.attributeValue("level").toUpperCase());
					Set<SkillLearn> learns = parseSkillLearn(classElement);
					for(ClassId classId2 : ClassId.VALUES)
						if(classId2.isOfLevel(classLevel))
							getHolder().addAllGeneralSkillLearns(classId2.getId(), learns);
				}
			}
		}
	}

	@Override
	protected void afterParseActions()
	{
		getHolder().initNormalSkillLearns();
		getHolder().initGeneralSkillLearns();
	}

	private Set<SkillLearn> parseSkillLearn(Element tree)
	{
		Set<SkillLearn> skillLearns = new HashSet<>();
		Iterator<Element> iterator = tree.elementIterator("skill");
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			int id = Integer.parseInt(element.attributeValue("id"));
			int level = element.attributeValue("level") == null ? 1 : Integer.parseInt(element.attributeValue("level"));
			int cost = element.attributeValue("cost") == null ? 0 : Integer.parseInt(element.attributeValue("cost"));
			int min_level = element.attributeValue("min_level") == null ? 1 : Integer.parseInt(element.attributeValue("min_level"));
			int item_id = element.attributeValue("item_id") == null ? 0 : Integer.parseInt(element.attributeValue("item_id"));
			long item_count = element.attributeValue("item_count") == null ? 1L : Long.parseLong(element.attributeValue("item_count"));
			boolean auto_get = element.attributeValue("auto_get") == null || Boolean.parseBoolean(element.attributeValue("auto_get"));
			Race race = element.attributeValue("race") == null ? null : Race.valueOf(element.attributeValue("race"));
			SkillLearn skillLearn = new SkillLearn(id, level, min_level, cost, item_id, item_count, auto_get, race);
			Condition condition = parseFirstCond(element);
			if(condition != null)
				skillLearn.addCondition(condition);
			skillLearns.add(skillLearn);
		}
		return skillLearns;
	}

	@Override
	protected Object getTableValue(String name, int... arg)
	{
		return null;
	}

	static
	{
		_instance = new SkillAcquireParser();
	}
}
