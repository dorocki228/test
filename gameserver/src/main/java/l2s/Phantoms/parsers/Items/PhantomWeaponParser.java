package  l2s.Phantoms.parsers.Items;


import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import org.dom4j.Element;

import  l2s.Phantoms.objects.sets.Weapons;
import  l2s.Phantoms.parsers.Items.holder.PhantomWeaponHolder;
import l2s.commons.data.xml.AbstractParser;
import  l2s.gameserver.Config;
import l2s.gameserver.templates.item.ItemGrade;

public class PhantomWeaponParser extends AbstractParser <PhantomWeaponHolder>
{
	private static final PhantomWeaponParser _instance = new PhantomWeaponParser();
	
	public static PhantomWeaponParser getInstance()
	{
		return _instance;
	}
	
	private PhantomWeaponParser()
	{
		super(PhantomWeaponHolder.getInstance());
	}
	
	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "config/Phantom/items/weapons/");
	}

	@Override
	public String getDTDFileName()
	{
		return "weapon.dtd";
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator <org.dom4j.Element> weaponIterator = rootElement.elementIterator(); weaponIterator.hasNext();)
		{
			org.dom4j.Element weaponElement = weaponIterator.next();
			int[] _class_Id = Arrays.stream(weaponElement.attributeValue("class_id").split(";")).mapToInt(Integer::parseInt).toArray();
			ItemGrade grade = ItemGrade.valueOf(weaponElement.attributeValue("grade"));
			
			int item_id = 0;
			
			for(Iterator <org.dom4j.Element> firstIterator = weaponElement.elementIterator(); firstIterator.hasNext();)
			{
				org.dom4j.Element firstElement = firstIterator.next();
				if (firstElement.getName().equalsIgnoreCase("item"))
				{
					item_id = Integer.valueOf(firstElement.attributeValue("item_id"));
				}
			}
			getHolder().addItems(new Weapons(_class_Id, grade,item_id));
		}
	}
	
}
