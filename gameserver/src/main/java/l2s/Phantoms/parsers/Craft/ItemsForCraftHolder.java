package  l2s.Phantoms.parsers.Craft;


import java.util.HashMap;
import java.util.Map;

import  l2s.commons.data.xml.AbstractHolder;
import  l2s.commons.util.Rnd;

public class ItemsForCraftHolder extends AbstractHolder
{
	private Map <Integer,CraftPhantom> _list = new HashMap <Integer,CraftPhantom>();
	
	public void addItems(int key, CraftPhantom itemList)
	{
		_list.put(key, itemList);
	}
	
	public int getRndKey()
	{
		if (_list.isEmpty() || _list.size() == 0)
			return -1;
		return Rnd.get(_list.keySet().stream().mapToInt(Integer::intValue).toArray());
	}
	
	public void DeleteItem(int key)
	{
		_list.remove(key);
	}
	
	public CraftPhantom getItem(int key)
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
	
	public static ItemsForCraftHolder getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private static class SingletonHolder
	{
		private static ItemsForCraftHolder instance = new ItemsForCraftHolder();
	}
	
}