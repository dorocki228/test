package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.commons.util.Rnd;
import l2s.gameserver.model.Zone;
import l2s.gameserver.templates.ZoneTemplate;
import l2s.gameserver.utils.ReflectionUtils;

import java.util.HashMap;
import java.util.Map;

public class ZoneHolder extends AbstractHolder
{
	private static final ZoneHolder _instance;
	private final Map<String, ZoneTemplate> _zones;

	public ZoneHolder()
	{
		_zones = new HashMap<>();
	}

	public static ZoneHolder getInstance()
	{
		return _instance;
	}

	public void addTemplate(ZoneTemplate zone)
	{
		String zoneName = zone.getName();
		if(_zones.containsKey(zoneName))
		{
			warn("Found duplicate zone: " + zoneName);
			return;
		}

		_zones.put(zoneName, zone);
	}

	public ZoneTemplate getTemplate(String name)
	{
		return _zones.get(name);
	}

	public Map<String, ZoneTemplate> getZones()
	{
		return _zones;
	}

	public Zone getRandomZone()
	{
		String _zoneName = "";
		int index = Rnd.get(1, getZones().size() - 1);
		int inx = 0;
		for(String tmp : getZones().keySet())
		{
			if(index == inx)
			{
				_zoneName = tmp;
				break;
			}
			++inx;
		}
		Zone zone = ReflectionUtils.getZone(_zoneName);
		if(zone == null)
		{
			System.out.println("null zone randomized");
			return getRandomZone();
		}
		return zone;
	}

	@Override
	public int size()
	{
		return _zones.size();
	}

	@Override
	public void clear()
	{
		_zones.clear();
	}

	static
	{
		_instance = new ZoneHolder();
	}
}
