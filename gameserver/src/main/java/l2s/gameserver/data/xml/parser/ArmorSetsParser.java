package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ArmorSetsHolder;
import l2s.gameserver.model.ArmorSet;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

public final class ArmorSetsParser extends AbstractParser<ArmorSetsHolder>
{
	private static final ArmorSetsParser _instance;

	public static ArmorSetsParser getInstance()
	{
		return _instance;
	}

	private ArmorSetsParser()
	{
		super(ArmorSetsHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/armor_sets.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "armor_sets.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			if("set".equalsIgnoreCase(element.getName()))
			{
				String[] chests = null;
                if(element.attributeValue("chests") != null)
					chests = element.attributeValue("chests").split(";");
                String[] legs = null;
                if(element.attributeValue("legs") != null)
					legs = element.attributeValue("legs").split(";");
                String[] head = null;
                if(element.attributeValue("head") != null)
					head = element.attributeValue("head").split(";");
                String[] gloves = null;
                if(element.attributeValue("gloves") != null)
					gloves = element.attributeValue("gloves").split(";");
                String[] feet = null;
                if(element.attributeValue("feet") != null)
					feet = element.attributeValue("feet").split(";");
                String[] shield = null;
                if(element.attributeValue("shield") != null)
					shield = element.attributeValue("shield").split(";");
                String[] shield_skills = null;
                if(element.attributeValue("shield_skills") != null)
					shield_skills = element.attributeValue("shield_skills").split(";");
                String[] enchant6skills = null;
                if(element.attributeValue("enchant6skills") != null)
					enchant6skills = element.attributeValue("enchant6skills").split(";");
                String[] enchant7skills = null;
                if(element.attributeValue("enchant7skills") != null)
					enchant7skills = element.attributeValue("enchant7skills").split(";");
                String[] enchant8skills = null;
                if(element.attributeValue("enchant8skills") != null)
					enchant8skills = element.attributeValue("enchant8skills").split(";");
				ArmorSet armorSet = new ArmorSet(chests, legs, head, gloves, feet, shield, shield_skills, enchant6skills, enchant7skills, enchant8skills);
				Iterator<Element> subIterator = element.elementIterator();
				while(subIterator.hasNext())
				{
					Element subElement = subIterator.next();
					if("set_skills".equalsIgnoreCase(subElement.getName()))
					{
						int partsCount = Integer.parseInt(subElement.attributeValue("parts"));
						String[] skills = subElement.attributeValue("skills").split(";");
						armorSet.addSkills(partsCount, skills);
					}
				}
				getHolder().addArmorSet(armorSet);
			}
		}
	}

	static
	{
		_instance = new ArmorSetsParser();
	}
}
