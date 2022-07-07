package l2s.Phantoms.parsers;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;

import l2s.Phantoms.enums.SpawnLocation;
import l2s.Phantoms.objects.LocationPhantom;
import l2s.commons.data.xml.AbstractParser;
import l2s.commons.geometry.Polygon;
import l2s.gameserver.Config;
import l2s.gameserver.model.Territory;
import l2s.gameserver.model.base.Fraction;
import l2s.commons.geometry.Polygon.PolygonBuilder;

public class LocationForCraftOrTradeParser extends AbstractParser<LocationForCraftOrTradeHolder>
{
	private static LocationForCraftOrTradeParser _instance = new LocationForCraftOrTradeParser();

	public static LocationForCraftOrTradeParser getInstance()
	{
		return _instance;
	}

	protected LocationForCraftOrTradeParser()
	{
		super(LocationForCraftOrTradeHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "config/Phantom/LocationForCraftOrTrade.xml");
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> spawnIterator = rootElement.elementIterator(); spawnIterator.hasNext();)
		{
			Element spawnElement = spawnIterator.next();
			if(spawnElement.getName().equalsIgnoreCase("territory"))
			{
				String terName = spawnElement.attributeValue("name");
				
				Territory territory = parseTerritory(terName, spawnElement);
				getHolder().add(new LocationPhantom(SpawnLocation.valueOf(terName), territory,Fraction.valueOf(spawnElement.attributeValue("fraction","NONE"))));
			}
		}
	}

	private Territory parseTerritory(String name, Element e)
	{
		Territory t = new Territory();
		t.add(parsePolygon0(name, e));

		for(Iterator<Element> iterator = e.elementIterator("banned_territory"); iterator.hasNext();)
			t.addBanned(parsePolygon0(name, iterator.next()));

		return t;
	}

	private Polygon parsePolygon0(String name, Element e)
	{
		PolygonBuilder temp = new PolygonBuilder();
		for(Iterator<Element> addIterator = e.elementIterator("add"); addIterator.hasNext();)
		{
			Element addElement = addIterator.next();
			int x = Integer.parseInt(addElement.attributeValue("x"));
			int y = Integer.parseInt(addElement.attributeValue("y"));
			int zmin = Integer.parseInt(addElement.attributeValue("zmin"));
			int zmax = Integer.parseInt(addElement.attributeValue("zmax"));
			temp.add(x, y).setZmin(zmin).setZmax(zmax);
		}

		if(!temp.validate())
			error("Invalid polygon: " + name + "{" + temp + "}. File: " + getCurrentFileName());
		return temp.createPolygon();
	}

	@Override
	public String getDTDFileName()
	{
		return "DTD/LocationForCraftOrTrade.dtd";
	}
}
