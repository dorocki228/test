package l2s.Phantoms.parsers.Trade;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import l2s.Phantoms.enums.SpawnLocation;
import l2s.Phantoms.enums.TypeOfShop;
import l2s.commons.data.xml.AbstractHolder;
import l2s.commons.util.Rnd;

public class ItemsInfoHolder extends AbstractHolder
{
	private Map <Integer,PhantomItemInfo> _list = new HashMap <Integer,PhantomItemInfo>();
	private Map <SpawnLocation,Integer> location_chance = new HashMap<SpawnLocation,Integer>();
	
	private int [] ratio = new int[2];
	
	public void addItems(int key, PhantomItemInfo itemList)
	{
		_list.put(key, itemList);
	}

	public List<PhantomItemInfo> getRdnItem(TypeOfShop type, SpawnLocation spawn_loc, int count)
	{
		List<PhantomItemInfo> tmp_lst = _list.values().stream().filter(i -> i.getSpawnLocation().contains(spawn_loc) && (type == TypeOfShop.SALE ? i.getSell_price_0()[0] > 0 : i.getBuy_price_0()[0] > 0)).collect(Collectors.toList());  
		Collections.shuffle(tmp_lst);
		if (_list.size()< count) 
			return Collections.emptyList();

		return  tmp_lst.stream().limit(count).collect(Collectors.toList());
	}
	
	public void DeleteItem(int key)
	{
		_list.remove(key);
	}
	
	public PhantomItemInfo getItem(int key)
	{
		return _list.get(key);
	}
	
	@Override
	public int size()
	{
		return _list.size();
	}
	
	@Override
	public void clear()
	{
		_list.clear();
	}
	
	public static ItemsInfoHolder getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private static class SingletonHolder
	{
		private static ItemsInfoHolder instance = new ItemsInfoHolder();
	}

	public SpawnLocation getLocationSpawn()
	{
		int chance = Rnd.get(1,100);	
		return Rnd.get(location_chance.entrySet().stream().filter(entry -> 100-entry.getValue() <= chance).map(Map.Entry::getKey).collect(Collectors.toList()));
	}
	
	public void addSpawnLoc(SpawnLocation value, int chance)
	{
		location_chance.put(value, chance);
	}

	public int [] getRatio()
	{
		return ratio;
	}

	public void setRatio(int [] ratio)
	{
		this.ratio = ratio;
	}
	
	
}