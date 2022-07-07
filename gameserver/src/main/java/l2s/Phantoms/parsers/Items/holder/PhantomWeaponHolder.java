package  l2s.Phantoms.parsers.Items.holder;


import java.util.ArrayList;
import java.util.List;

import  l2s.Phantoms.PhantomVariables;
import  l2s.Phantoms.objects.sets.Weapons;
import  l2s.commons.data.xml.AbstractHolder;
import  l2s.commons.util.Rnd;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.model.base.ClassId;
import l2s.gameserver.templates.item.ItemGrade;

public class PhantomWeaponHolder extends AbstractHolder
{
	private static List <Weapons> _weapon = new ArrayList <Weapons>();
	
	public Weapons getRndWeapon(Player fantom, ItemGrade grade)
	{
		List <Weapons> list = new ArrayList <Weapons>();
		ItemGrade db_grade = ItemGrade.valueOf(PhantomVariables.getString("MaxEquipGrade", "S"));
		
		for(Weapons set : _weapon) // выбрать подходящие сеты по профе и грейду
			if (set.getClassId(fantom.getBaseClassId()) && set.getGrade().ordinal() == grade.ordinal() && set.getWeapon() != 0)
				list.add(set);

		if (!list.isEmpty())
			return Rnd.get(list);
			else if (db_grade != ItemGrade.S) // список пуст и включено ограничение
			{
				// проверим "родителя"
				ClassId parent = fantom.getClassId().getParent();
				if(parent!=null)
					for(Weapons set : _weapon) // выбрать подходящие сеты по профе и грейду
						if (set.getClassId(parent.getId()) && set.getGrade().ordinal() == grade.ordinal() && set.getWeapon() != 0)
							list.add(set);
			}
		
		if (!list.isEmpty())
			return Rnd.get(list);
		else// ничего не нашли - выводим лог
			_log.warn("Weapons is Empty garade: "+grade+" class_id:"+fantom.getBaseClassId()+" "+fantom);

		return null;
	}
	
	public List <Weapons> getWeapon(Player phantom, ItemGrade grade)
	{
		List <Weapons> list = new ArrayList <Weapons>();
		
		ItemGrade db_grade = ItemGrade.valueOf(PhantomVariables.getString("MaxEquipGrade", "S"));
		if (db_grade.ordinal() <  grade.ordinal())
			grade = db_grade;
		
		for(Weapons set : _weapon) // выбрать подходящие сеты по профе и грейду
		{
			if (set.getClassId(phantom.getBaseClassId()) && set.getGrade().ordinal() == grade.ordinal() && set.getWeapon() != 0)
				list.add(set);
		}
		if (list.isEmpty())
		{
			// проверим "родителя"
			ClassId parent = phantom.getClassId().getParent();
			if(parent!=null)
			for(Weapons set : _weapon) // выбрать подходящие сеты по профе и грейду
			{
				if (set.getClassId(parent.getId()) && set.getGrade().ordinal() == grade.ordinal() && set.getWeapon() != 0)
					list.add(set);
			}
		}
		return list;
	}
	
	public void addItems(Weapons template)
	{
		_weapon.add(template);
	}
	
	public Weapons getRndItems()
	{
		return _weapon.get(Rnd.get(_weapon.size()));
	}
	
	@Override
	public int size()
	{
		return _weapon.size();
	}
	
	@Override
	public void clear()
	{
		_weapon.clear();
	}
	
	public static PhantomWeaponHolder getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private static class SingletonHolder
	{
		private static PhantomWeaponHolder instance = new PhantomWeaponHolder();
	}
	
}