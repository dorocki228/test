package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.PlayerTemplateHolder;
import l2s.gameserver.model.base.ClassType;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.base.Sex;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.StartItem;
import l2s.gameserver.templates.player.HpMpCpData;
import l2s.gameserver.templates.player.PlayerTemplate;
import l2s.gameserver.utils.Location;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class PlayerTemplateParser extends AbstractParser<PlayerTemplateHolder>
{
	private static final PlayerTemplateParser _instance = new PlayerTemplateParser();

    public static PlayerTemplateParser getInstance()
	{
		return _instance;
	}

	private PlayerTemplateParser()
	{
		super(PlayerTemplateHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/pc_parameters/template_data/");
	}

	@Override
	public String getDTDFileName()
	{
		return "template_data.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			Race race = Race.valueOf(element.attributeValue("race").toUpperCase());
			Sex sex = Sex.valueOf(element.attributeValue("sex").toUpperCase());
			ClassType classtype = ClassType.valueOf(element.attributeValue("type").toUpperCase());
			StatsSet set = new StatsSet();
			Iterator<Element> subIterator = element.elementIterator();
			while(subIterator.hasNext())
			{
				Element subElement = subIterator.next();
				if("stats_data".equalsIgnoreCase(subElement.getName()))
					for(Element e : subElement.elements())
						if("min_attributes".equalsIgnoreCase(e.getName()) || "max_attributes".equalsIgnoreCase(e.getName()) || "base_attributes".equalsIgnoreCase(e.getName()))
						{
							int _int = Integer.parseInt(e.attributeValue("int"));
							int str = Integer.parseInt(e.attributeValue("str"));
							int con = Integer.parseInt(e.attributeValue("con"));
							int men = Integer.parseInt(e.attributeValue("men"));
							int dex = Integer.parseInt(e.attributeValue("dex"));
							int wit = Integer.parseInt(e.attributeValue("wit"));
							if("min_attributes".equalsIgnoreCase(e.getName()))
							{
								set.set("minINT", _int);
								set.set("minSTR", str);
								set.set("minCON", con);
								set.set("minMEN", men);
								set.set("minDEX", dex);
								set.set("minWIT", wit);
							}
							else if("max_attributes".equalsIgnoreCase(e.getName()))
							{
								set.set("maxINT", _int);
								set.set("maxSTR", str);
								set.set("maxCON", con);
								set.set("maxMEN", men);
								set.set("maxDEX", dex);
								set.set("maxWIT", wit);
							}
							else
							{
								if(!"base_attributes".equalsIgnoreCase(e.getName()))
									continue;
								set.set("baseINT", _int);
								set.set("baseSTR", str);
								set.set("baseCON", con);
								set.set("baseMEN", men);
								set.set("baseDEX", dex);
								set.set("baseWIT", wit);
							}
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
							if(!"base_stats".equalsIgnoreCase(e.getName()))
								continue;
							for(Element e2 : e.elements())
								if("set".equalsIgnoreCase(e2.getName()))
									set.set(e2.attributeValue("name"), e2.attributeValue("value"));
						}
			}
			PlayerTemplate template = new PlayerTemplate(set, race, sex);
			Iterator<Element> subIterator2 = element.elementIterator();
			while(subIterator2.hasNext())
			{
				Element subElement2 = subIterator2.next();
				if("creation_data".equalsIgnoreCase(subElement2.getName()))
				{
					for(Element e3 : subElement2.elements())
						if("start_equipments".equalsIgnoreCase(e3.getName()))
						{
							for(Element e4 : e3.elements())
								if("equipment".equalsIgnoreCase(e4.getName()))
								{
									int item_id = Integer.parseInt(e4.attributeValue("item_id"));
									long count = Long.parseLong(e4.attributeValue("count"));
									boolean equiped = Boolean.parseBoolean(e4.attributeValue("equiped"));
									int enchant_level = e4.attributeValue("enchant_level") == null ? 0 : Integer.parseInt(e4.attributeValue("enchant_level"));
									template.addStartItem(new StartItem(item_id, count, equiped, enchant_level));
								}
						}
						else
						{
							if(!"start_points".equalsIgnoreCase(e3.getName()))
								continue;
							for(Element e4 : e3.elements())
								if("point".equalsIgnoreCase(e4.getName()))
									template.addStartLocation(Location.parse(e4));
						}
				}
				else
				{
					if(!"stats_data".equalsIgnoreCase(subElement2.getName()))
						continue;
					for(Element e3 : subElement2.elements())
						if("base_stats".equalsIgnoreCase(e3.getName()))
							for(Element e4 : e3.elements())
								if("regen_data".equalsIgnoreCase(e4.getName()))
									for(Element e5 : e4.elements())
										if("regen".equalsIgnoreCase(e5.getName()))
										{
											int level = Integer.parseInt(e5.attributeValue("level"));
											double hp = Double.parseDouble(e5.attributeValue("hp"));
											double mp = Double.parseDouble(e5.attributeValue("mp"));
											double cp = Double.parseDouble(e5.attributeValue("cp"));
											template.addRegenData(level, new HpMpCpData(hp, mp, cp));
										}
				}
			}
			getHolder().addPlayerTemplate(race, classtype, sex, template);
		}
	}

}
