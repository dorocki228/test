package l2s.Phantoms.parsers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.extended.EncodedByteArrayConverter;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;

import l2s.Phantoms.Utils.PhantomUtils;
import l2s.Phantoms.enums.RouteType;
import l2s.Phantoms.objects.TrafficScheme.PhantomRoute;
import l2s.gameserver.Config;
import l2s.gameserver.model.base.Fraction;

public class PhantomRouteParser
{
	private final Logger _log = LoggerFactory.getLogger(PhantomRouteParser.class);
	private static PhantomRouteParser _instance = new PhantomRouteParser();

	private static XStream xStream;

	public static PhantomRouteParser getInstance()
	{
		if(_instance == null)
		{
			_instance = new PhantomRouteParser();
		}
		return _instance;
	}

	public List<PhantomRoute> _route_list = new ArrayList<PhantomRoute>();
	public List<PhantomRoute> _solo_route_list = new ArrayList<PhantomRoute>();
	
	public PhantomRouteParser()
	{
		xStream = new XStream(new StaxDriver(new XmlFriendlyNameCoder("_-", "_")));
		xStream.allowTypesByRegExp(new String[] { ".*" });
		xStream.alias("list", List.class);
		xStream.alias("route", PhantomRoute.class);
		xStream.autodetectAnnotations(true);
		xStream.registerConverter((Converter) new EncodedByteArrayConverter());

		loadPhantomRoute();
	}

	public void addNewRoute(PhantomRoute ts)
	{
		_route_list.add(ts);
	}
	
	public List<PhantomRoute> getAllPhantomSoloRoute()
	{
		return _solo_route_list;
	}
	
	public List<PhantomRoute> getAllPhantomRoute()
	{
		return _route_list;
	}

	public PhantomRoute getSchemeByName(String name)
	{
		for(PhantomRoute tmp : _route_list)
		{
			if(PhantomUtils.equals(name, tmp.getName()))
				return tmp;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public void loadPhantomRoute()
	{
		try
		{
			Collection<File> files = FileUtils.listFiles(new File(Config.DATAPACK_ROOT, "config/Phantom/Routes/"), FileFilterUtils.suffixFileFilter(".xml"), FileFilterUtils.directoryFileFilter());
			for(File f : files)
				if(!f.isHidden())
					try
					{
						_route_list.addAll((List<PhantomRoute>) xStream.fromXML(f));
						for(PhantomRoute r:_route_list)
						{
							if(r.getType() == RouteType.START)
							_solo_route_list.add(r);
						}
					}
					catch(Exception e)
					{
						_log.info("Exception: " + e + " in file: " + f.getName(), e);
					}
		}
		catch(Exception e)
		{
			_log.warn("Exception: " + e, e);
		}

		_log.info("Phantom Traffic Scheme: " + _route_list.size());
	}

	public void delete(File file)
	{
		if(!file.exists())
			return;
		if(file.isDirectory())
		{
			for(File f : file.listFiles())
				delete(f);
			file.delete();
		}
		else
		{
			file.delete();
		}
	}

	public void SavePhantomRoute()
	{
		_log.info("Phantom Traffic Scheme: saving started...");

		_route_list.forEach(ts -> {
			ts.pathClean();
		});
		// чистим от битых и пустых
		_route_list = _route_list.stream().filter(d -> d != null && d.getPointsFirstTask() != null && d.getPointsFirstTask().size() > 30 && (d.getType() ==  RouteType.START || d.getType() == RouteType.SOLO) /*&& !d.incorrectStartPoint()*/).collect(Collectors.toList());

		List<PhantomRoute> standart_TS = _route_list.stream().filter(d -> d != null && !d.isPlayer()&& d.getType() != RouteType.START).collect(Collectors.toList());
		standart_TS.forEach(ts -> {
			if(ts.getName() == null || ts.getName().isEmpty())
			{
				ts.setName(UUID.randomUUID().toString().replace("-", ""));
			}
		});
		delete(new File("config/Phantom/Routes/Player/"));
		delete(new File("config/Phantom/Routes/"));

		saveTStoFile(standart_TS, "config/Phantom/Routes/", "PhantomRoute.xml");

		List<PhantomRoute> start_TS = _route_list.stream().filter(d -> d != null&& d.getType() == RouteType.START ).collect(Collectors.toList());
		start_TS.forEach(s-> {s.setType(RouteType.START); s.setFraction(Fraction.NONE); s.setLvl(0); s.setClassId(0);});
		
		saveTStoFile(start_TS, "config/Phantom/Routes/", "StartRoute.xml");
		
		List<PhantomRoute> player_TS = _route_list.stream().filter(d -> d != null && d.isPlayer()&& d.checkFaction() && d.getType() != RouteType.START).collect(Collectors.toList());

		Map<String, List<PhantomRoute>> result = player_TS.stream().collect(Collectors.groupingBy(r -> r.getName().split("_")[0]));
		for(Map.Entry<String, List<PhantomRoute>> tmp : result.entrySet())
		{
			saveTStoFile(tmp.getValue(), "config/Phantom/Routes/Player/", tmp.getKey() + ".xml");
		}
		_log.info("Phantom Traffic Scheme: Saved.");

	}

	private void saveTStoFile(List<PhantomRoute> _TS, String folder, String file)
	{
		File theDir = new File(folder);
		if(!theDir.exists())
			theDir.mkdirs();

		// JAVA OBJECT --> XML
		String xml = xStream.toXML(_TS);
		try
		{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(folder + file), StandardCharsets.UTF_8));
			bufferedWriter.write(xml);

			bufferedWriter.close();
		}
		catch(IOException e1)
		{
			e1.printStackTrace();
		}
	}

}
