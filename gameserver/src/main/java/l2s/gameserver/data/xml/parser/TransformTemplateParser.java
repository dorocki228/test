package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.commons.string.StringArrayUtils;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.TransformTemplateHolder;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.Sex;
import l2s.gameserver.model.base.TransformType;
import l2s.gameserver.model.items.LockType;
import l2s.gameserver.templates.BaseStatsBonus;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.player.HpMpCpData;
import l2s.gameserver.templates.player.transform.TransformTemplate;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class TransformTemplateParser extends AbstractParser<TransformTemplateHolder>
{
	private static final TransformTemplateParser _instance;

	public static TransformTemplateParser getInstance()
	{
		return _instance;
	}

	private TransformTemplateParser()
	{
		super(TransformTemplateHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/pc_parameters/transform_data/");
	}

	@Override
	public String getDTDFileName()
	{
		return "transform_data.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			int id = Integer.parseInt(element.attributeValue("id"));
			TransformType type = TransformType.valueOf(element.attributeValue("type").toUpperCase());
			boolean can_swim = Boolean.parseBoolean(element.attributeValue("can_swim"));
			int spawn_height = element.attributeValue("spawn_height") == null ? 0 : Integer.parseInt(element.attributeValue("spawn_height"));
			boolean normal_attackable = Boolean.parseBoolean(element.attributeValue("normal_attackable"));
			Iterator<Element> sexIterator = element.elementIterator();
			while(sexIterator.hasNext())
			{
				Element sexElement = sexIterator.next();
				if("male".equalsIgnoreCase(sexElement.getName()) || "female".equalsIgnoreCase(sexElement.getName()))
				{
					StatsSet set = TransformTemplate.getEmptyStatsSet();
					Sex sex = Sex.valueOf(sexElement.getName().toUpperCase());
					set.set("id", id);
					set.set("type", type);
					set.set("can_swim", can_swim);
					set.set("spawn_height", spawn_height);
					set.set("normal_attackable", normal_attackable);
					set.set("sex", sex);
					for(Element e : sexElement.elements())
						if("base_attributes".equalsIgnoreCase(e.getName()))
						{
							set.set("baseINT", Integer.parseInt(e.attributeValue("int")));
							set.set("baseSTR", Integer.parseInt(e.attributeValue("str")));
							set.set("baseCON", Integer.parseInt(e.attributeValue("con")));
							set.set("baseMEN", Integer.parseInt(e.attributeValue("men")));
							set.set("baseDEX", Integer.parseInt(e.attributeValue("dex")));
							set.set("baseWIT", Integer.parseInt(e.attributeValue("wit")));
						}
						else if("armor_defence".equalsIgnoreCase(e.getName()))
						{
							set.set("baseChestDef", e.attributeValue("chest"));
							set.set("baseLegsDef", e.attributeValue("legs"));
							set.set("baseHelmetDef", e.attributeValue("helmet"));
							set.set("baseBootsDef", e.attributeValue("boots"));
							set.set("baseGlovesDef", e.attributeValue("gloves"));
							set.set("basePendantDef", e.attributeValue("pendant"));
							set.set("baseCloakDef", e.attributeValue("cloak"));
						}
						else if("jewel_defence".equalsIgnoreCase(e.getName()))
						{
							set.set("baseREarDef", e.attributeValue("r_earring"));
							set.set("baseLEarDef", e.attributeValue("l_earring"));
							set.set("baseRRingDef", e.attributeValue("r_ring"));
							set.set("baseLRingDef", e.attributeValue("l_ring"));
							set.set("baseNecklaceDef", e.attributeValue("necklace"));
						}
						else
						{
							if(!"set".equalsIgnoreCase(e.getName()))
								continue;
							set.set(e.attributeValue("name"), e.attributeValue("value"));
						}
					TransformTemplate template = new TransformTemplate(set);
					for(Element e2 : sexElement.elements())
						if("actions".equalsIgnoreCase(e2.getName()))
						{
							String[] split;
							String[] actions = split = e2.getText().split(" ");
							for(String action : split)
								template.addAction(Integer.parseInt(action));
						}
						else if("item_check".equalsIgnoreCase(e2.getName()))
						{
							LockType check_action = LockType.valueOf(e2.attributeValue("action").toUpperCase());
							int[] check_items = StringArrayUtils.stringToIntArray(e2.getText(), " ");
							template.setItemCheck(check_action, check_items);
						}
						else if("skills".equalsIgnoreCase(e2.getName()))
						{
							Iterator<Element> skillIterator = e2.elementIterator("skill");
							while(skillIterator.hasNext())
							{
								Element skillElement = skillIterator.next();
								int skill_id = Integer.parseInt(skillElement.attributeValue("id"));
								int skill_level = skillElement.attributeValue("level") == null ? 1 : Integer.parseInt(skillElement.attributeValue("level"));
								template.addSkill(new SkillLearn(skill_id, skill_level, 0, 0, 0, 0L, false, null));
							}
						}
						else if("additional_skills".equalsIgnoreCase(e2.getName()))
						{
							Iterator<Element> skillIterator = e2.elementIterator("skill");
							while(skillIterator.hasNext())
							{
								Element skillElement = skillIterator.next();
								int skill_id = Integer.parseInt(skillElement.attributeValue("id"));
								int skill_level = skillElement.attributeValue("level") == null ? 1 : Integer.parseInt(skillElement.attributeValue("level"));
								int skill_min_level = skillElement.attributeValue("min_level") == null ? 1 : Integer.parseInt(skillElement.attributeValue("min_level"));
								template.addAddtionalSkill(new SkillLearn(skill_id, skill_level, skill_min_level, 0, 0, 0L, false, null));
							}
						}
						else if("base_stats_bonus".equalsIgnoreCase(e2.getName()))
						{
							for(Element e3 : e2.elements())
								if("bonus".equalsIgnoreCase(e3.getName()))
								{
									int value = Integer.parseInt(e3.attributeValue("value"));
									int str = Integer.parseInt(e3.attributeValue("str"));
									int dex = Integer.parseInt(e3.attributeValue("dex"));
									int con = Integer.parseInt(e3.attributeValue("con"));
									int _int = Integer.parseInt(e3.attributeValue("int"));
									int men = Integer.parseInt(e3.attributeValue("men"));
									int wit = Integer.parseInt(e3.attributeValue("wit"));
									template.addBaseStatsBonus(value, new BaseStatsBonus(_int, str, con, men, dex, wit));
								}
						}
						else
						{
							if(!"level_data".equalsIgnoreCase(e2.getName()))
								continue;
							for(Element e3 : e2.elements())
								if("level".equalsIgnoreCase(e3.getName()))
								{
									int value = Integer.parseInt(e3.attributeValue("value"));
									double mod = Double.parseDouble(e3.attributeValue("mod"));
									double hp = Double.parseDouble(e3.attributeValue("hp"));
									double mp = Double.parseDouble(e3.attributeValue("mp"));
									double cp = Double.parseDouble(e3.attributeValue("cp"));
									double hp_regen = Double.parseDouble(e3.attributeValue("hp_regen"));
									double mp_regen = Double.parseDouble(e3.attributeValue("mp_regen"));
									double cp_regen = Double.parseDouble(e3.attributeValue("cp_regen"));
									template.addLevelBonus(value, mod);
									template.addHpMpCpData(value, new HpMpCpData(hp, mp, cp));
									template.addRegenData(value, new HpMpCpData(hp_regen, mp_regen, cp_regen));
								}
						}
					getHolder().addTemplate(sex, template);
				}
			}
		}
	}

	static
	{
		_instance = new TransformTemplateParser();
	}
}
