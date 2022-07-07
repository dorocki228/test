package  l2s.Phantoms.parsers.Items;


import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;

import  l2s.Phantoms.objects.sets.UnderwearSet;
import  l2s.Phantoms.parsers.Items.holder.PhantomUnderwearHolder;
import l2s.commons.data.xml.AbstractParser;
import  l2s.gameserver.Config;
import l2s.gameserver.templates.item.ItemGrade;

public class PhantomUnderwearParser extends AbstractParser <PhantomUnderwearHolder>
{
	private static final PhantomUnderwearParser _instance = new PhantomUnderwearParser();
	
	public static PhantomUnderwearParser getInstance()
	{
		return _instance;
	}
	
	private PhantomUnderwearParser()
	{
		super(PhantomUnderwearHolder.getInstance());
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator <org.dom4j.Element> armorIterator = rootElement.elementIterator(); armorIterator.hasNext();)
		{
			org.dom4j.Element armorElement = armorIterator.next();
			ItemGrade grade = ItemGrade.valueOf(armorElement.attributeValue("grade"));
			
			int shirt = 0;
			int belt = 0;
			for(Iterator <org.dom4j.Element> firstIterator = armorElement.elementIterator(); firstIterator.hasNext();)
			{
				org.dom4j.Element firstElement = firstIterator.next();
				if (firstElement.getName().equalsIgnoreCase("shirt"))
				{
					shirt = Integer.valueOf(firstElement.attributeValue("item_id"));
				}
				if (firstElement.getName().equalsIgnoreCase("belt"))
				{
					belt = Integer.valueOf(firstElement.attributeValue("item_id"));
				}
			}
			getHolder().addItems(new UnderwearSet(grade, shirt, belt));
		}
	}
	
	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "config/Phantom/items/underwear/");
	}
	
	@Override
	public String getDTDFileName()
	{
		return "underwear.dtd";
	}
}
