package  l2s.Phantoms.parsers.Items.holder;


import java.util.ArrayList;
import java.util.List;
import  l2s.Phantoms.objects.sets.UnderwearSet;
import  l2s.commons.data.xml.AbstractHolder;
import  l2s.commons.util.Rnd;
import l2s.gameserver.templates.item.ItemGrade;

public class PhantomUnderwearHolder extends AbstractHolder
{
	private ArrayList <UnderwearSet> _Underwearlist = new ArrayList <UnderwearSet>();
	
	public UnderwearSet getUnderwearSet(ItemGrade grade)
	{
		List <UnderwearSet> list = new ArrayList<>();
		for(UnderwearSet set : _Underwearlist) // выбрать подходящие сеты по грейду
		{
			if (set.getGrade().ordinal() == grade.ordinal())
				list.add(set);
		}
		if (list.size() > 0)
			return Rnd.get(list);
		
		return null;
	}
	
	public void addItems(UnderwearSet itemList)
	{
		_Underwearlist.add(itemList);
	}
	
	@Override
	public int size()
	{
		return _Underwearlist.size();
	}
	
	@Override
	public void clear()
	{
		_Underwearlist.clear();
	}
	
	public static PhantomUnderwearHolder getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private static class SingletonHolder
	{
		private static PhantomUnderwearHolder instance = new PhantomUnderwearHolder();
	}
	
}