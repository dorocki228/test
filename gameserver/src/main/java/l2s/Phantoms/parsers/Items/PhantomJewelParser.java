package  l2s.Phantoms.parsers.Items;


import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import org.dom4j.Element;

import  l2s.Phantoms.objects.sets.JewelSet;
import  l2s.Phantoms.parsers.Items.holder.PhantomJewelHolder;
import l2s.commons.data.xml.AbstractParser;
import  l2s.gameserver.Config;
import l2s.gameserver.templates.item.ItemGrade;

public class PhantomJewelParser extends AbstractParser <PhantomJewelHolder>
{
	private static final PhantomJewelParser _instance = new PhantomJewelParser();
	
	public static PhantomJewelParser getInstance()
	{
		return _instance;
	}
	
	private PhantomJewelParser()
	{
		super(PhantomJewelHolder.getInstance());
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator <org.dom4j.Element> armorIterator = rootElement.elementIterator(); armorIterator.hasNext();)
		{
			org.dom4j.Element armorElement = armorIterator.next();
			int[] _class_Id = Arrays.stream(armorElement.attributeValue("class_id").split(";")).mapToInt(Integer::parseInt).toArray();
			ItemGrade grade = ItemGrade.valueOf(armorElement.attributeValue("grade"));

			int earring_left = 0;
			int earring_right = 0;
			int ring_left = 0;
			int ring_right = 0;
			int necklace = 0;
			
			for(Iterator <org.dom4j.Element> firstIterator = armorElement.elementIterator(); firstIterator.hasNext();)
			{
				org.dom4j.Element firstElement = firstIterator.next();
				if (firstElement.getName().equalsIgnoreCase("earring_left"))
				{
					earring_left = Integer.valueOf(firstElement.attributeValue("item_id"));
				}
				if (firstElement.getName().equalsIgnoreCase("earring_right"))
				{
					earring_right = Integer.valueOf(firstElement.attributeValue("item_id"));
				}
				if (firstElement.getName().equalsIgnoreCase("ring_left"))
				{
					ring_left = Integer.valueOf(firstElement.attributeValue("item_id"));
				}
				if (firstElement.getName().equalsIgnoreCase("ring_right"))
				{
					ring_right = Integer.valueOf(firstElement.attributeValue("item_id"));
				}
				if (firstElement.getName().equalsIgnoreCase("necklace"))
				{
					necklace = Integer.valueOf(firstElement.attributeValue("item_id"));
				}
			}

			getHolder().addItems(new JewelSet(_class_Id,grade,earring_left,earring_right,ring_left,ring_right,necklace));
		}
	}
	
	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "config/Phantom/items/jewel/");
	}
	
	@Override
	public String getDTDFileName()
	{
		return "jewel.dtd";
	}
	
}
