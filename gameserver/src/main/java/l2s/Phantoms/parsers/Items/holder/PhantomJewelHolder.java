package  l2s.Phantoms.parsers.Items.holder;


import java.util.ArrayList;
import java.util.List;

import  l2s.Phantoms.PhantomVariables;
import  l2s.Phantoms.objects.sets.JewelSet;
import  l2s.commons.data.xml.AbstractHolder;
import  l2s.commons.util.Rnd;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.model.base.ClassId;
import l2s.gameserver.templates.item.ItemGrade;

public class PhantomJewelHolder extends AbstractHolder
{
	private List <JewelSet> _Jewellist = new ArrayList <JewelSet>();
	
	public List <JewelSet> getJewelSet(Player phantom, ItemGrade grade)
	{
		List <JewelSet> list = new ArrayList <JewelSet>();
		
		ItemGrade db_grade = ItemGrade.valueOf(PhantomVariables.getString("MaxEquipGrade", "S"));
		if (db_grade.ordinal() <  grade.ordinal())
			grade = db_grade;
		
		
		for(JewelSet set : _Jewellist) // выбрать подходящие сеты по профе и грейду
		{
			if (set.getClassId(phantom.getBaseClassId()) && set.getGrade().ordinal() == grade.ordinal())
				list.add(set);
		}
		if (list.isEmpty())
		{
			// проверим "родителя"
			ClassId parent = phantom.getClassId().getParent();
			if(parent!=null)
			for(JewelSet set : _Jewellist) // выбрать подходящие сеты по профе и грейду
			{
				if (set.getClassId(parent.getId()) && set.getGrade().ordinal() == grade.ordinal())
					list.add(set);
			}
		}
		return list;
	}
	
	public JewelSet getJewelRndSet(Player fantom, ItemGrade grade)
	{
		List <JewelSet> list = new ArrayList <JewelSet>();
		ItemGrade db_grade = ItemGrade.valueOf(PhantomVariables.getString("MaxEquipGrade", "S"));
	
		
		for(JewelSet set : _Jewellist) // выбрать подходящие сеты по профе и грейду
				//проверим основную профу
				if (set.getClassId(fantom.getBaseClassId()) && set.getGrade().ordinal() == grade.ordinal())
					list.add(set);
		
		if (!list.isEmpty())
			return Rnd.get(list);
		else if (db_grade != ItemGrade.S) // список пуст и включено ограничение
		{
			// проверим "родителя"
			ClassId parent = fantom.getClassId().getParent();
			if(parent!=null)
			for(JewelSet set : _Jewellist) // выбрать подходящие сеты по профе и грейду
				if (set.getClassId(parent.getId()) && set.getGrade().ordinal() == grade.ordinal())
					list.add(set);
		}
		
		if (!list.isEmpty())
			return Rnd.get(list);
		else// ничего не нашли - выводим лог
			_log.warn("Jewel Set is Empty garade:"+grade+" class_id:"+fantom.getBaseClassId()+" "+fantom);

		return null;
	}
	
	public void addItems(JewelSet itemList)
	{
		_Jewellist.add(itemList);
	}
	
	@Override
	public int size()
	{
		return _Jewellist.size();
	}
	
	@Override
	public void clear()
	{
		_Jewellist.clear();
	}
	
	public static PhantomJewelHolder getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private static class SingletonHolder
	{
		private static PhantomJewelHolder instance = new PhantomJewelHolder();
	}
	
}