package l2s.Phantoms.parsers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import l2s.Phantoms.enums.SpawnLocation;
import l2s.Phantoms.objects.LocationPhantom;
import l2s.commons.data.xml.AbstractHolder;
import l2s.commons.util.Rnd;

public class LocationForCraftOrTradeHolder extends AbstractHolder
{
	private List<LocationPhantom> territories_all = new ArrayList<LocationPhantom>();
	
	public void add(LocationPhantom territory)
	{
			territories_all.add(territory);
	}

	public LocationPhantom getLocationCraft(SpawnLocation _loc)
	{
		List<LocationPhantom> list = territories_all.stream().filter(t ->  t.getName() == _loc).collect(Collectors.toList());
		
		if(!list.isEmpty())
			return Rnd.get(list);
		else
			_log.warn("Location Craft is Empty " + " Location:" + _loc);

		return null;
	}

	@Override
	public int size()
	{
		return territories_all.size();
	}

	@Override
	public void clear()
	{
		territories_all.clear();;
	}

	public static LocationForCraftOrTradeHolder getInstance()
	{
		return SingletonHolder.instance;
	}

	private static class SingletonHolder
	{
		private static LocationForCraftOrTradeHolder instance = new LocationForCraftOrTradeHolder();
	}

}