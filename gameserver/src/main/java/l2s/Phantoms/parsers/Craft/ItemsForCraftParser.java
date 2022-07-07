package  l2s.Phantoms.parsers.Craft;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import  l2s.Phantoms.enums.SpawnLocation;
import l2s.commons.data.xml.AbstractParser;
import  l2s.gameserver.Config;

public class ItemsForCraftParser extends AbstractParser <ItemsForCraftHolder>
{
	private static ItemsForCraftParser _instance = new ItemsForCraftParser();
	
	public static ItemsForCraftParser getInstance()
	{
		return _instance;
	}
	
	protected ItemsForCraftParser()
	{
		super(ItemsForCraftHolder.getInstance());
	}
	
	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "config/Phantom/ItemsForCraft.xml");
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		int key = 0;
		for(Element surveyElement : rootElement.elements("items"))
		{
			key++;
			
			final List <Integer> recipesId = new ArrayList <Integer>();
			final List <Integer> prices = new ArrayList <Integer>();
			
			String Craftname = surveyElement.attributeValue("signboard");
			SpawnLocation loc = SpawnLocation.valueOf(surveyElement.attributeValue("town"));
			int count = 0;
			
			for(Element itemElement : surveyElement.elements("item"))
			{
				count = count+1;
				recipesId.add(Integer.parseInt(itemElement.attributeValue("RecipesId")));
				prices.add(Integer.parseInt(itemElement.attributeValue("Prices")));
			}
			
			getHolder().addItems(key, new CraftPhantom(recipesId, prices, count, Craftname, loc));
		}
	}
	
	@Override
	public String getDTDFileName()
	{
		return "DTD/ItemsForCraft.dtd";
	}
}
