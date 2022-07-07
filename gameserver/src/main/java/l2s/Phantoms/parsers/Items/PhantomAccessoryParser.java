package  l2s.Phantoms.parsers.Items;


import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;

import  l2s.Phantoms.objects.sets.AccessorySet;
import  l2s.Phantoms.parsers.Items.holder.PhantomAccessoryHolder;
import l2s.commons.data.xml.AbstractParser;
import  l2s.gameserver.Config;
import  l2s.gameserver.templates.StatsSet;

public class PhantomAccessoryParser extends AbstractParser <PhantomAccessoryHolder>
{
	private static final PhantomAccessoryParser _instance = new PhantomAccessoryParser();
	
	public static PhantomAccessoryParser getInstance()
	{
		return _instance;
	}
	
	private PhantomAccessoryParser()
	{
		super(PhantomAccessoryHolder.getInstance());
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator <org.dom4j.Element> armorIterator = rootElement.elementIterator(); armorIterator.hasNext();)
		{
			org.dom4j.Element armorElement = armorIterator.next();
			String class_id = armorElement.attributeValue("class_id");
			StatsSet set = new StatsSet();
			set.set("class_Id", class_id);
			
			for(Iterator <org.dom4j.Element> firstIterator = armorElement.elementIterator(); firstIterator.hasNext();)
			{
				org.dom4j.Element firstElement = firstIterator.next();
				if (firstElement.getName().equalsIgnoreCase("male_hat"))
				{
					set.set("male_hat", firstElement.attributeValue("item_id"));
				}
				if (firstElement.getName().equalsIgnoreCase("female_hat"))
				{
					set.set("female_hat", firstElement.attributeValue("item_id"));
				}
				if (firstElement.getName().equalsIgnoreCase("cloak"))
				{
					set.set("cloak", firstElement.attributeValue("item_id"));
					set.set("cloak_chance", firstElement.attributeValue("chance"));
				}
			}
			AccessorySet template = new AccessorySet(set);
			
			getHolder().addItems(template);
		}
	}
	
	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "config/Phantom/items/accessory/");
	}
	
	@Override
	public String getDTDFileName()
	{
		return "Accessory.dtd";
	}
	
}
