package  l2s.Phantoms.parsers.Items;


import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import org.dom4j.Element;

import  l2s.Phantoms.objects.sets.ArmorSet;
import  l2s.Phantoms.parsers.Items.holder.PhantomArmorHolder;
import l2s.commons.data.xml.AbstractParser;
import  l2s.gameserver.Config;
import l2s.gameserver.templates.item.ItemGrade;
public class PhantomArmorParser extends AbstractParser <PhantomArmorHolder>
{
	private static final PhantomArmorParser _instance = new PhantomArmorParser();
	
	public static PhantomArmorParser getInstance()
	{
		return _instance;
	}
	
	private PhantomArmorParser()
	{
		super(PhantomArmorHolder.getInstance());
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		
		for(Iterator <org.dom4j.Element> armorIterator = rootElement.elementIterator(); armorIterator.hasNext();)
		{
			org.dom4j.Element armorElement = armorIterator.next();
			int[] class_id = Arrays.stream(armorElement.attributeValue("class_id").split(";")).mapToInt(Integer::parseInt).toArray();
			ItemGrade grade = ItemGrade.valueOf(armorElement.attributeValue("grade"));
			
			int _helm = 0;
			int _chest = 0;
			int _gaiter = 0;
			int _gloves = 0;
			int _boots = 0;
			int _shield = 0;
			
			for(Iterator subIterator = armorElement.elementIterator(); subIterator.hasNext();)
			{
				org.dom4j.Element subElement = (org.dom4j.Element) subIterator.next();
				String subName = subElement.getName();
				if (subName.equalsIgnoreCase("helm"))
				{
					_helm = Integer.valueOf(subElement.attributeValue("item_id"));
				}
				if (subName.equalsIgnoreCase("chest"))
					_chest = Integer.valueOf(subElement.attributeValue("item_id"));
				
				if (subName.equalsIgnoreCase("gaiter"))
					_gaiter = Integer.valueOf(subElement.attributeValue("item_id"));
				
				if (subName.equalsIgnoreCase("gloves"))
					_gloves = Integer.valueOf(subElement.attributeValue("item_id"));
				
				if (subName.equalsIgnoreCase("boots"))
					_boots = Integer.valueOf(subElement.attributeValue("item_id"));
				
				if (subName.equalsIgnoreCase("shield"))
					_shield = Integer.valueOf(subElement.attributeValue("item_id"));
				
			}
			getHolder().addItems(new ArmorSet (class_id, grade, _helm, _chest, _gaiter, _gloves, _boots, _shield));
		}
	}
	
	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "config/Phantom/items/armor/");
	}
	
	@Override
	public String getDTDFileName()
	{
		return "armor.dtd";
	}
}
