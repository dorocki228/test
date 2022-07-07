package  l2s.Phantoms.templates;


import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import  l2s.commons.util.Rnd;

public class ItemsGroup
{
	@XStreamAlias("item")
	@XStreamImplicit 
	private List <PhantomItem> items = new ArrayList <PhantomItem>();
	
	public void addItem(PhantomItem item)
	{
		items.add(item);
	}
	
	public PhantomItem getRandomItems()
	{
		return Rnd.get(items);
	}
	
	public List <PhantomItem> getAllItems()
	{
		return items;
	}
}
