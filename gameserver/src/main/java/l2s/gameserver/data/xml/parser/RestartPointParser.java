package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.commons.geometry.Polygon;
import l2s.commons.geometry.Rectangle;
import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.MapRegionManager;
import l2s.gameserver.model.Territory;
import l2s.gameserver.model.World;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.templates.mapregion.RestartArea;
import l2s.gameserver.templates.mapregion.RestartPoint;
import l2s.gameserver.utils.Location;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.dom4j.Attribute;
import org.dom4j.Element;

import java.io.File;
import java.util.*;

public class RestartPointParser extends AbstractParser<MapRegionManager>
{
	private static final RestartPointParser _instance;

	public static RestartPointParser getInstance()
	{
		return _instance;
	}

	private RestartPointParser()
	{
		super(MapRegionManager.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/mapregion/restart_points.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "restart_points.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		List<Pair<Territory, Map<Race, String>>> restartArea = new ArrayList<>();
		Map<String, RestartPoint> restartPoint = new HashMap<>();
		Iterator<Element> iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			Element listElement = iterator.next();
			if("restart_area".equals(listElement.getName()))
			{
				Territory territory = null;
				Map<Race, String> restarts = new HashMap<>();
				Iterator<Element> i = listElement.elementIterator();
				while(i.hasNext())
				{
					Element n = i.next();
					if("region".equalsIgnoreCase(n.getName()))
					{
						Attribute map = n.attribute("map");
						String s = map.getValue();
						String[] val = s.split("_");
						int rx = Integer.parseInt(val[0]);
						int ry = Integer.parseInt(val[1]);
						int x1 = World.MAP_MIN_X + (rx - Config.GEO_X_FIRST << 15);
						int y1 = World.MAP_MIN_Y + (ry - Config.GEO_Y_FIRST << 15);
						int x2 = x1 + 32768 - 1;
						int y2 = y1 + 32768 - 1;
						Rectangle shape = new Rectangle(x1, y1, x2, y2);
						shape.setZmin(World.MAP_MIN_Z);
						shape.setZmax(World.MAP_MAX_Z);
						if(territory == null)
							territory = new Territory();
						territory.add(shape);
					}
					else if("polygon".equalsIgnoreCase(n.getName()))
					{
						Polygon.PolygonBuilder shape2 = ZoneParser.parsePolygon(n);
						if(!shape2.validate())
                            error("RestartPointParser: invalid territory data : " + shape2 + "!");
						if(territory == null)
							territory = new Territory();
						territory.add(shape2.createPolygon());
					}
					else
					{
						if(!"restart".equalsIgnoreCase(n.getName()))
							continue;
						Race race = Race.valueOf(n.attributeValue("race").toUpperCase());
						String locName = n.attributeValue("loc");
						restarts.put(race, locName);
					}
				}
				if(territory == null)
					throw new RuntimeException("RestartPointParser: empty territory!");
				if(restarts.isEmpty())
					throw new RuntimeException("RestartPointParser: restarts not defined!");
				restartArea.add(new ImmutablePair(territory, restarts));
			}
			else
			{
				if(!"restart_loc".equals(listElement.getName()))
					continue;
				String name = listElement.attributeValue("name");
				int bbs = Integer.parseInt(listElement.attributeValue("bbs", "0"));
				int msgId = Integer.parseInt(listElement.attributeValue("msg_id", "0"));
				List<Location> restartPoints = new ArrayList<>();
				List<Location> PKrestartPoints = new ArrayList<>();
				Iterator<Element> j = listElement.elementIterator();
				while(j.hasNext())
				{
					Element n2 = j.next();
					if("restart_point".equals(n2.getName()))
					{
						Iterator<Element> ii = n2.elementIterator();
						while(ii.hasNext())
						{
							Element d = ii.next();
							if("coords".equalsIgnoreCase(d.getName()))
							{
								Location loc = Location.parseLoc(d.attribute("loc").getValue());
								restartPoints.add(loc);
							}
						}
					}
					else
					{
						if(!"PKrestart_point".equals(n2.getName()))
							continue;
						Iterator<Element> ii = n2.elementIterator();
						while(ii.hasNext())
						{
							Element d = ii.next();
							if("coords".equalsIgnoreCase(d.getName()))
							{
								Location loc = Location.parseLoc(d.attribute("loc").getValue());
								PKrestartPoints.add(loc);
							}
						}
					}
				}
				if(restartPoints.isEmpty())
					throw new RuntimeException("RestartPointParser: restart_points not defined for restart_loc : " + name + "!");
				if(PKrestartPoints.isEmpty())
					PKrestartPoints = restartPoints;
				RestartPoint rp = new RestartPoint(name, bbs, msgId, restartPoints, PKrestartPoints);
				restartPoint.put(name, rp);
			}
		}
		for(Pair<Territory, Map<Race, String>> ra : restartArea)
		{
			Map<Race, RestartPoint> restarts2 = new HashMap<>();
			for(Map.Entry<Race, String> e : ra.getValue().entrySet())
			{
				RestartPoint rp2 = restartPoint.get(e.getValue());
				if(rp2 == null)
					throw new RuntimeException("RestartPointParser: restart_loc not found : " + e.getValue() + "!");
				restarts2.put(e.getKey(), rp2);
				getHolder().addRegionData(new RestartArea(ra.getKey(), restarts2));
			}
		}
	}

	static
	{
		_instance = new RestartPointParser();
	}
}
