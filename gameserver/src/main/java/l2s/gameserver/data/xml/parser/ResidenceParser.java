package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ResidenceFunctionsHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.ResidenceFunctionType;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.entity.residence.ResidenceFunction;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.support.MerchantGuard;
import l2s.gameserver.templates.residence.ResidenceFunctionTemplate;
import l2s.gameserver.utils.Location;
import org.dom4j.Attribute;
import org.dom4j.Element;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Iterator;

public final class ResidenceParser extends AbstractParser<ResidenceHolder>
{
	private static final ResidenceParser _instance;

	public static ResidenceParser getInstance()
	{
		return _instance;
	}

	private ResidenceParser()
	{
		super(ResidenceHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/residences/");
	}

	@Override
	public String getDTDFileName()
	{
		return "residence.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			String impl = element.attributeValue("impl");
            StatsSet set = new StatsSet();
			Iterator<Attribute> subIterator = element.attributeIterator();
			while(subIterator.hasNext())
			{
				Attribute subElement = subIterator.next();
				set.set(subElement.getName(), subElement.getValue());
			}
			Residence residence = null;
			try
			{
                Class<?> clazz = Class.forName("l2s.gameserver.model.entity.residence." + impl);
                Constructor<?> constructor = clazz.getConstructor(StatsSet.class);
				residence = (Residence) constructor.newInstance(set);
				getHolder().addResidence(residence);
			}
			catch(Exception e)
			{
                error("fail to init: " + getCurrentFileName(), e);
				return;
			}
			if("residence".equalsIgnoreCase(element.getName()))
			{
				Iterator<Element> subIterator2 = element.elementIterator();
				while(subIterator2.hasNext())
				{
					Element subElement2 = subIterator2.next();
					String nodeName = subElement2.getName();
					if("available_functions".equalsIgnoreCase(nodeName))
					{
						Iterator<Element> nextIterator = subElement2.elementIterator();
						while(nextIterator.hasNext())
						{
							Element nextElement = nextIterator.next();
							ResidenceFunctionType type = ResidenceFunctionType.valueOf(nextElement.attributeValue("type").toUpperCase());
							int level = Integer.parseInt(nextElement.attributeValue("level"));
							ResidenceFunctionTemplate template = ResidenceFunctionsHolder.getInstance().getTemplate(type, level);
							if(template == null)
								continue;
							residence.addAvailableFunction(template.getId());
						}
					}
					else if("skills".equalsIgnoreCase(nodeName))
					{
						Iterator<Element> nextIterator = subElement2.elementIterator();
						while(nextIterator.hasNext())
						{
							Element nextElement = nextIterator.next();
							int id2 = Integer.parseInt(nextElement.attributeValue("id"));
							int level2 = Integer.parseInt(nextElement.attributeValue("level"));
							Skill skill = SkillHolder.getInstance().getSkill(id2, level2);
							if(skill != null)
								residence.addSkill(skill);
						}
					}
					else if("banish_points".equalsIgnoreCase(nodeName))
					{
						Iterator<Element> banishPointsIterator = subElement2.elementIterator();
						while(banishPointsIterator.hasNext())
						{
							Location loc = Location.parse(banishPointsIterator.next());
							residence.addBanishPoint(loc);
						}
					}
					else if("owner_restart_points".equalsIgnoreCase(nodeName))
					{
						Iterator<Element> ownerRestartPointsIterator = subElement2.elementIterator();
						while(ownerRestartPointsIterator.hasNext())
						{
							Location loc = Location.parse(ownerRestartPointsIterator.next());
							residence.addOwnerRestartPoint(loc);
						}
					}
					else if("other_restart_points".equalsIgnoreCase(nodeName))
					{
						Iterator<Element> otherRestartPointsIterator = subElement2.elementIterator();
						while(otherRestartPointsIterator.hasNext())
						{
							Location loc = Location.parse(otherRestartPointsIterator.next());
							residence.addOtherRestartPoint(loc);
						}
					}
					else if("chaos_restart_points".equalsIgnoreCase(nodeName))
					{
						Iterator<Element> chaosRestartPointsIterator = subElement2.elementIterator();
						while(chaosRestartPointsIterator.hasNext())
						{
							Location loc = Location.parse(chaosRestartPointsIterator.next());
							residence.addChaosRestartPoint(loc);
						}
					}
					else
					{
						if(!"merchant_guards".equalsIgnoreCase(nodeName))
							continue;
						Iterator<Element> thirdElementIterator = subElement2.elementIterator();
						while(thirdElementIterator.hasNext())
						{
							Element thirdElement = thirdElementIterator.next();
							int itemId = Integer.parseInt(thirdElement.attributeValue("item_id"));
							int npcId2 = Integer.parseInt(thirdElement.attributeValue("npc_id"));
							int maxGuard = Integer.parseInt(thirdElement.attributeValue("max"));
							((Castle) residence).addMerchantGuard(new MerchantGuard(itemId, npcId2, maxGuard));
						}
					}
				}
			}
			else
			{
				if(!"instant_residence".equalsIgnoreCase(element.getName()))
					continue;
				Iterator<Element> subIterator2 = element.elementIterator();
				while(subIterator2.hasNext())
				{
					Element subElement2 = subIterator2.next();
					String nodeName = subElement2.getName();
					if("functions".equalsIgnoreCase(nodeName))
					{
						Iterator<Element> nextIterator = subElement2.elementIterator("function");
						while(nextIterator.hasNext())
						{
							Element nextElement = nextIterator.next();
							ResidenceFunctionType type = ResidenceFunctionType.valueOf(nextElement.attributeValue("type").toUpperCase());
							int level = Integer.parseInt(nextElement.attributeValue("level"));
							ResidenceFunctionTemplate template = ResidenceFunctionsHolder.getInstance().getTemplate(type, level);
							if(template == null)
								continue;
							residence.addActiveFunction(new ResidenceFunction(template, residence.getId()));
						}
					}
				}
			}
		}
	}

	static
	{
		_instance = new ResidenceParser();
	}
}
