package l2s.gameserver.data.xml.parser;

import gve.zones.model.GveOutpost;
import l2s.commons.data.xml.AbstractParser;
import l2s.commons.geometry.Circle;
import l2s.commons.geometry.Polygon;
import l2s.commons.geometry.Polygon.PolygonBuilder;
import l2s.commons.geometry.Rectangle;
import l2s.commons.geometry.Shape;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ZoneHolder;
import l2s.gameserver.model.Territory;
import l2s.gameserver.model.World;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.ZoneTemplate;
import l2s.gameserver.utils.Location;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class ZoneParser extends AbstractParser<ZoneHolder>
{
	private static final ZoneParser _instance = new ZoneParser();

	public static ZoneParser getInstance()
	{
		return _instance;
	}

	protected ZoneParser()
	{
		super(ZoneHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/zone/");
	}

    @Override
	public String getDTDFileName()
	{
		return "zone.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
            Element zoneElement = iterator.next();
			if("zone".equals(zoneElement.getName()))
			{
                StatsSet zoneDat = new StatsSet();
                zoneDat.set("name", zoneElement.attribute("name").getValue());
				zoneDat.set("type", zoneElement.attribute("type").getValue());
				Territory territory = null;
				Iterator<Element> i = zoneElement.elementIterator();
				while(i.hasNext())
				{
					Element n = i.next();
					if("set".equals(n.getName()))
						zoneDat.set(n.attributeValue("name"), n.attributeValue("val"));
					else if("restart_point".equals(n.getName()))
					{
						List<Location> restartPoints = new ArrayList<>();
						Iterator<Element> ii = n.elementIterator();
						while(ii.hasNext())
						{
							Element d = ii.next();
							if("coords".equalsIgnoreCase(d.getName()))
							{
								Location loc = Location.parseLoc(d.attribute("loc").getValue());
								restartPoints.add(loc);
							}
						}
						zoneDat.set("restart_points", restartPoints);
					}
					else if("PKrestart_point".equals(n.getName()))
					{
						List<Location> PKrestartPoints = new ArrayList<>();
						Iterator<Element> ii = n.elementIterator();
						while(ii.hasNext())
						{
							Element d = ii.next();
							if("coords".equalsIgnoreCase(d.getName()))
							{
								Location loc = Location.parseLoc(d.attribute("loc").getValue());
								PKrestartPoints.add(loc);
							}
						}
						zoneDat.set("PKrestart_points", PKrestartPoints);
					}
					else if("outposts".equals(n.getName()))
					{
						List<GveOutpost> outposts = new ArrayList<>();
						Iterator<Element> ii = n.elementIterator();
						while(ii.hasNext())
						{
							Element d = ii.next();
							if("outpost".equalsIgnoreCase(d.getName()))
							{
								String name = d.attribute("name").getValue();
								Optional<GveOutpost> outpost = GveOutpost.find(name);

								outpost.ifPresentOrElse(outposts::add,
										() -> error("Can't find outpost " + name));
							}
						}
						zoneDat.set("outposts", outposts);
					}
					else if("in_game_name".equals(n.getName()))
						zoneDat.set("in_game_name", n.attributeValue("val"));
					else
					{
						boolean isShape;
						if((isShape = "rectangle".equalsIgnoreCase(n.getName())) || "banned_rectangle".equalsIgnoreCase(n.getName()))
						{
							Shape shape = parseRectangle(n);
							if(territory == null)
							{
								territory = new Territory();
								zoneDat.set("territory", territory);
							}
							if(isShape)
								territory.add(shape);
							else
								territory.addBanned(shape);
						}
						else if((isShape = "circle".equalsIgnoreCase(n.getName())) || "banned_cicrcle".equalsIgnoreCase(n.getName()))
						{
							Shape shape = parseCircle(n);
							if(territory == null)
							{
								territory = new Territory();
								zoneDat.set("territory", territory);
							}
							if(isShape)
								territory.add(shape);
							else
								territory.addBanned(shape);
						}
						else if((isShape = "polygon".equalsIgnoreCase(n.getName())) || "banned_polygon".equalsIgnoreCase(n.getName()))
						{
							PolygonBuilder shape2 = parsePolygon(n);
							if(!shape2.validate())
                                error("ZoneParser: invalid territory data : " + shape2 + ", zone: " + zoneDat.getString("name") + "!");
							if(territory == null)
							{
								territory = new Territory();
								zoneDat.set("territory", territory);
							}
							Polygon polygon = shape2.createPolygon();
							if(isShape)
                                territory.add(polygon);
							else
								territory.addBanned(polygon);
						}
						else
						{
							if(!(isShape = "map".equalsIgnoreCase(n.getName())) && !"banned_map".equalsIgnoreCase(n.getName()))
								continue;
							Shape shape = parseMap(n);
							if(territory == null)
							{
								territory = new Territory();
								zoneDat.set("territory", territory);
							}
							if(isShape)
								territory.add(shape);
							else
								territory.addBanned(shape);
						}
					}
				}
				if(territory == null || territory.getTerritories().isEmpty())
                    error("Empty territory for zone: " + zoneDat.get("name"));
				ZoneTemplate template = new ZoneTemplate(zoneDat);
				getHolder().addTemplate(template);
			}
		}
	}

	public static Rectangle parseRectangle(Element n)
    {
		int zmin = World.MAP_MIN_Z;
		int zmax = World.MAP_MAX_Z;
		Iterator<Element> i = n.elementIterator();
		Element d = i.next();
		String[] coord = d.attributeValue("loc").split("[\\s,;]+");
		int x1 = Integer.parseInt(coord[0]);
		int y1 = Integer.parseInt(coord[1]);
		if(coord.length > 2)
		{
			zmin = Integer.parseInt(coord[2]);
			zmax = Integer.parseInt(coord[3]);
		}
		d = i.next();
		coord = d.attributeValue("loc").split("[\\s,;]+");
		int x2 = Integer.parseInt(coord[0]);
		int y2 = Integer.parseInt(coord[1]);
		if(coord.length > 2)
		{
			zmin = Integer.parseInt(coord[2]);
			zmax = Integer.parseInt(coord[3]);
		}
		Rectangle rectangle = new Rectangle(x1, y1, x2, y2);
		rectangle.setZmin(zmin);
		rectangle.setZmax(zmax);
		return rectangle;
	}

	public static PolygonBuilder parsePolygon(Element shape)
	{
		PolygonBuilder polygonBuilder = new PolygonBuilder();
		Iterator<Element> i = shape.elementIterator();
		while(i.hasNext())
		{
			Element d = i.next();
			if("coords".equals(d.getName()))
			{
				String[] coord = d.attributeValue("loc").split("[\\s,;]+");
				if(coord.length < 3)
					polygonBuilder.add(Integer.parseInt(coord[0]), Integer.parseInt(coord[1])).setZmin(World.MAP_MIN_Z).setZmax(World.MAP_MAX_Z);
				else if(coord.length < 4)
					polygonBuilder.add(Integer.parseInt(coord[0]), Integer.parseInt(coord[1])).setZmin(Integer.parseInt(coord[2])).setZmax(World.MAP_MAX_Z);
				else
					polygonBuilder.add(Integer.parseInt(coord[0]), Integer.parseInt(coord[1])).setZmin(Integer.parseInt(coord[2])).setZmax(Integer.parseInt(coord[3]));
			}
		}

		return polygonBuilder;
	}

	public static Circle parseCircle(Element shape)
    {
		String[] coord = shape.attribute("loc").getValue().split("[\\s,;]+");
		Circle circle;
		if(coord.length < 4)
			circle = new Circle(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]), Integer.parseInt(coord[2])).setZmin(World.MAP_MIN_Z).setZmax(World.MAP_MAX_Z);
		else if(coord.length < 5)
			circle = new Circle(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]), Integer.parseInt(coord[2])).setZmin(Integer.parseInt(coord[3])).setZmax(World.MAP_MAX_Z);
		else
			circle = new Circle(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]), Integer.parseInt(coord[2])).setZmin(Integer.parseInt(coord[3])).setZmax(Integer.parseInt(coord[4]));
		return circle;
	}

	public static Rectangle parseMap(Element n)
    {
		String[] map = n.attributeValue("value").split("_");
		int rx = Integer.parseInt(map[0]);
		int ry = Integer.parseInt(map[1]);
		int x1 = World.MAP_MIN_X + (rx - Config.GEO_X_FIRST << 15);
		int y1 = World.MAP_MIN_Y + (ry - Config.GEO_Y_FIRST << 15);
		int x2 = x1 + 32768 - 1;
		int y2 = y1 + 32768 - 1;
		Rectangle rectangle = new Rectangle(x1, y1, x2, y2);
		rectangle.setZmin(World.MAP_MIN_Z);
		rectangle.setZmax(World.MAP_MAX_Z);
		return rectangle;
	}
}
