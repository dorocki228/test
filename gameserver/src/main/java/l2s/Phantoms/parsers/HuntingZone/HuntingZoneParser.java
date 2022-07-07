package  l2s.Phantoms.parsers.HuntingZone;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import l2s.commons.geometry.Polygon;
import l2s.commons.geometry.Polygon.PolygonBuilder;

import  l2s.Phantoms.parsers.HuntingZone.Converter.ClassIdConverter;
import  l2s.Phantoms.parsers.HuntingZone.Converter.PenaltyWeaponConverter;
import  l2s.Phantoms.parsers.HuntingZone.Converter.RaceEnumConverter;
import  l2s.gameserver.model.Territory;
import  l2s.gameserver.model.World;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HuntingZoneParser
{
	private final Logger _log = LoggerFactory.getLogger(HuntingZoneParser.class);
	private static HuntingZoneParser _instance = new HuntingZoneParser();
	
	public static HuntingZoneParser getInstance()
	{
		if (_instance == null)
		{
			_instance = new HuntingZoneParser();
		}
		return _instance;
	}
	
	public void Save()
	{
		try
		{
			_log.info("HuntingZoneParser: saving started...");
			marshaller(HuntingZoneHolder.getInstance().getAll());
			_log.info("HuntingZoneParser: Saved.");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public PolygonBuilder parsePolygon(List<String> cords)
	{
		PolygonBuilder poly = new PolygonBuilder();
		for (String i : cords)
		{
			String[] coord = i.split("[\\s,;]+");
			if (coord.length < 4) // Не указаны minZ и maxZ, берем граничные значения
				poly.add(Integer.parseInt(coord[0]), Integer.parseInt(coord[1])).setZmin(World.MAP_MIN_Z).setZmax(World.MAP_MAX_Z);
			else
				poly.add(Integer.parseInt(coord[0]), Integer.parseInt(coord[1])).setZmin(Integer.parseInt(coord[2])).setZmax(Integer.parseInt(coord[3]));
		}
		return poly;
	}
	
	public void load()
	{
		List<HuntingZone> HZN = null;
		try
		{
			HZN = unmarshalling(new File("config/Phantom/HuntingZone.xml"));
		} catch (ClassNotFoundException | IOException e)
		{
			e.printStackTrace();
		}
		for (HuntingZone tmp : HZN)
		{
			HuntingZoneHolder.getInstance().addItems(init(tmp));
		}
		_log.info("Load " + HuntingZoneHolder.getInstance().getAll().size() + " Hunting Zone");
	}
	
	private HuntingZone init(HuntingZone tmp)
	{
		HuntingZone ret = tmp;
		List<Territory> _list_territory = new ArrayList<Territory>();
		
		if (tmp.getPolygon() != null && !tmp.getPolygon().isEmpty())
			for (HuntingZonePolygon i : tmp.getPolygon())
			{
				PolygonBuilder shape = parsePolygon(i.coords);
				if (shape.validate())
					_list_territory.add(new Territory().add(shape.createPolygon()));
			}
		ret.setTerritoryList(_list_territory);
		return ret;
	}
	
	// чтение
	@SuppressWarnings("unchecked")
	public List<HuntingZone> unmarshalling(File file) throws IOException, ClassNotFoundException
	{
		XStream xStream = new XStream(new DomDriver("UTF-8"));
		xStream.allowTypesByRegExp(new String[] { ".*" });
		xStream.alias("list", List.class);
		xStream.alias("zone", HuntingZone.class);
		
		xStream.aliasAttribute(HuntingZone.class, "id", "id");
		xStream.aliasAttribute(HuntingZone.class, "name", "name");
		xStream.aliasAttribute(HuntingZone.class, "lvlMin", "lvlMin");
		xStream.aliasAttribute(HuntingZone.class, "lvlMax", "lvlMax");
		xStream.aliasAttribute(HuntingZone.class, "maxPartySize", "maxPartySize");
		xStream.aliasAttribute(HuntingZone.class, "playerCheckRadius", "playerCheckRadius");
		
		xStream.registerLocalConverter(HuntingZone.class, "race", new RaceEnumConverter());
		xStream.registerLocalConverter(HuntingZone.class, "penaltyWeapon", new PenaltyWeaponConverter());
		xStream.registerLocalConverter(HuntingZone.class, "classId", new ClassIdConverter());
		
		xStream.processAnnotations(HuntingZone.class);
		
		return (List<HuntingZone>) xStream.fromXML(file);
	}
	
	// запись
	public void marshaller(List<HuntingZone> list) throws IOException
	{
		XStream xStream = new XStream(new DomDriver("UTF-8"));
		xStream.allowTypesByRegExp(new String[] { ".*" });
		xStream.alias("list", List.class);
		xStream.alias("zone", HuntingZone.class);
		
		xStream.aliasAttribute(HuntingZone.class, "id", "id");
		xStream.aliasAttribute(HuntingZone.class, "name", "name");
		xStream.aliasAttribute(HuntingZone.class, "lvlMin", "lvlMin");
		xStream.aliasAttribute(HuntingZone.class, "lvlMax", "lvlMax");
		xStream.aliasAttribute(HuntingZone.class, "maxPartySize", "maxPartySize");
		xStream.aliasAttribute(HuntingZone.class, "playerCheckRadius", "playerCheckRadius");
		
		xStream.registerLocalConverter(HuntingZone.class, "race", new RaceEnumConverter());
		xStream.registerLocalConverter(HuntingZone.class, "penaltyWeapon", new PenaltyWeaponConverter());
		xStream.registerLocalConverter(HuntingZone.class, "classId", new ClassIdConverter());
		
		xStream.processAnnotations(HuntingZone.class);
		String xml = xStream.toXML(list);
		saveToFile(xml);
	}
	
	private void saveToFile(String xml) throws IOException
	{
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File("config/Phantom/HuntingZone.xml"), StandardCharsets.UTF_8));
		bufferedWriter.write(xml);
		bufferedWriter.close();
	}
}
