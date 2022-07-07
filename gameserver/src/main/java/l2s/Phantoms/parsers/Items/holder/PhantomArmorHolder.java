package  l2s.Phantoms.parsers.Items.holder;


import java.util.ArrayList;
import java.util.List;

import  l2s.Phantoms.PhantomVariables;
import  l2s.Phantoms.enums.PhantomType;
import  l2s.Phantoms.objects.sets.ArmorSet;
import  l2s.commons.data.xml.AbstractHolder;
import  l2s.commons.util.Rnd;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.model.base.ClassId;
import l2s.gameserver.templates.item.ItemGrade;

public class PhantomArmorHolder extends AbstractHolder
{
	private static List <ArmorSet> _armors = new ArrayList <ArmorSet>();
	
	public List <ArmorSet> getArmorSet(Player phantom, ItemGrade grade)
	{
		List <ArmorSet> list = new ArrayList <ArmorSet>();
		ItemGrade db_grade = ItemGrade.valueOf(PhantomVariables.getString("MaxEquipGrade", "S"));
		if (db_grade.ordinal() <  grade.ordinal())
			grade = db_grade;
		
		for(ArmorSet set : _armors) // выбрать подходящие сеты по профе и грейду
		{
			if (set.getClassId(phantom.getBaseClassId()) && set.getGrade().ordinal() == grade.ordinal() && set.getChest() != 0)
				list.add(set);
		}
		if (list.isEmpty())
		{
			// проверим "родителя"
			ClassId parent = phantom.getClassId().getParent();
			if(parent!=null)
			for(ArmorSet set : _armors) // выбрать подходящие сеты по профе и грейду
				if (set.getClassId(parent.getId()) && set.getGrade().ordinal() == grade.ordinal() && set.getChest() != 0)
					list.add(set);
		}
		
		return list;
	}
	
	public ArmorSet getSetByChest(Player phantom, ItemGrade grade, int Chest)
	{
		List <ArmorSet> list = new ArrayList <ArmorSet>();
		ItemGrade db_grade = ItemGrade.valueOf(PhantomVariables.getString("MaxEquipGrade", "S"));
		if (db_grade.ordinal() <  grade.ordinal())
			grade = db_grade;
		
		for(ArmorSet set : _armors) // выбрать подходящие сеты по профе и грейду
		{
			if (set.getClassId(phantom.getBaseClassId()) && set.getGrade().ordinal() == grade.ordinal() && set.getChest() != 0 && set.getChest() == Chest)
				list.add(set);
		}
		if (!list.isEmpty())
		{
			return Rnd.get(list);
		}
		else
		{
			_log.warn("Armor Set is Empty garade:"+grade+" class_id:"+phantom.getBaseClassId());
		}
		
		return null;
	}
	
	public ArmorSet getArmorRndSet(Player fantom,  ItemGrade grade)
	{
		List <ArmorSet> list = new ArrayList <ArmorSet>();
		ItemGrade db_grade = ItemGrade.valueOf(PhantomVariables.getString("MaxEquipGrade", "S"));
		
		for(ArmorSet set : _armors) // выбрать подходящие сеты по профе и грейду
			if (set.getClassId(fantom.getBaseClassId()) && set.getGrade().ordinal() == grade.ordinal() && set.getChest() != 0)
				list.add(set);
		
		if (!list.isEmpty())
			return Rnd.get(list);
		else if (db_grade != ItemGrade.S) // список пуст и включено ограничение
		{
			// проверим "родителя"
			ClassId parent = fantom.getClassId().getParent();
			if(parent!=null)
			for(ArmorSet set : _armors) // выбрать подходящие сеты по профе и грейду
				if (set.getClassId(parent.getId()) && set.getGrade().ordinal() == grade.ordinal() && set.getChest() != 0)
					list.add(set);
		} 
		
		if (!list.isEmpty())
			return Rnd.get(list);
		else// ничего не нашли - выводим лог
			_log.warn("Armor Set is Empty garade:"+grade+" class_id:"+fantom.getBaseClassId()+" "+fantom.toString() + " grade:" + grade.name());

		return null;
	}
	
	public void addItems(ArmorSet template)
	{
		_armors.add(template);
	}
	
	@Override
	public int size()
	{
		return _armors.size();
	}
	
	@Override
	public void clear()
	{
		_armors.clear();
	}
	
	public static PhantomArmorHolder getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private static class SingletonHolder
	{
		private static PhantomArmorHolder instance = new PhantomArmorHolder();
	}
	
}