package  l2s.Phantoms.parsers.Items.holder;


import java.util.ArrayList;
import java.util.List;

import  l2s.Phantoms.objects.sets.AccessorySet;
import  l2s.commons.data.xml.AbstractHolder;
import  l2s.commons.util.Rnd;

public class PhantomAccessoryHolder extends AbstractHolder
{
	private List <AccessorySet> _list = new ArrayList <AccessorySet>();
	
	public void addItems(AccessorySet template)
	{
		_list.add(template);
	}
	
	public AccessorySet getAccessorySet(int class_id)
	{
		List <AccessorySet> list = new ArrayList <AccessorySet>();
		for(AccessorySet set : _list) // выбрать подходящие сеты по профе и грейду
		{
			if (set.getClassId(class_id))
				list.add(set);
		}
		if (!list.isEmpty())
		{
			return Rnd.get(list);
		}
		
		return null;
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
	
	public static PhantomAccessoryHolder getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private static class SingletonHolder
	{
		private static PhantomAccessoryHolder instance = new PhantomAccessoryHolder();
	}
	
}