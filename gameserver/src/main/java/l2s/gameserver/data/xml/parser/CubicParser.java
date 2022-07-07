package l2s.gameserver.data.xml.parser;

import gnu.trove.map.hash.TIntIntHashMap;
import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.CubicHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.CubicTemplate;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class CubicParser extends AbstractParser<CubicHolder>
{
	private static final CubicParser _instance;

	public static CubicParser getInstance()
	{
		return _instance;
	}

	protected CubicParser()
	{
		super(CubicHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/cubics.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "cubics.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element cubicElement = iterator.next();
			int id = Integer.parseInt(cubicElement.attributeValue("id"));
			int level = Integer.parseInt(cubicElement.attributeValue("level"));
			int duration = Integer.parseInt(cubicElement.attributeValue("duration"));
			int delay = Integer.parseInt(cubicElement.attributeValue("delay"));
			CubicTemplate template = new CubicTemplate(id, level, duration, delay);
			getHolder().addCubicTemplate(template);
			Iterator<Element> skillsIterator = cubicElement.elementIterator();
			while(skillsIterator.hasNext())
			{
				Element skillsElement = skillsIterator.next();
				int chance = Integer.parseInt(skillsElement.attributeValue("chance"));
				List<CubicTemplate.SkillInfo> skills = new ArrayList<>(1);
				Iterator<Element> skillIterator = skillsElement.elementIterator();
				while(skillIterator.hasNext())
				{
					Element skillElement = skillIterator.next();
					int id2 = Integer.parseInt(skillElement.attributeValue("id"));
					int level2 = Integer.parseInt(skillElement.attributeValue("level"));
					int chance2 = skillElement.attributeValue("chance") == null ? 0 : Integer.parseInt(skillElement.attributeValue("chance"));
					boolean canAttackDoor = Boolean.parseBoolean(skillElement.attributeValue("can_attack_door"));
					CubicTemplate.ActionType type = CubicTemplate.ActionType.valueOf(skillElement.attributeValue("action_type"));
					TIntIntHashMap set = new TIntIntHashMap();
					Iterator<Element> chanceIterator = skillElement.elementIterator();
					while(chanceIterator.hasNext())
					{
						Element chanceElement = chanceIterator.next();
						int min = Integer.parseInt(chanceElement.attributeValue("min"));
						int max = Integer.parseInt(chanceElement.attributeValue("max"));
						int value = Integer.parseInt(chanceElement.attributeValue("value"));
						for(int i = min; i <= max; ++i)
							set.put(i, value);
					}
					if(chance2 == 0 && set.isEmpty())
                        warn("Wrong skill chance. Cubic: " + id + "/" + level);
					Skill skill = SkillHolder.getInstance().getSkill(id2, level2);
					if(skill != null)
					{
						skill.setCubicSkill(true);
						skills.add(new CubicTemplate.SkillInfo(skill, chance2, type, canAttackDoor, set));
					}
				}
				template.putSkills(chance, skills);
			}
		}
	}

	static
	{
		_instance = new CubicParser();
	}
}
