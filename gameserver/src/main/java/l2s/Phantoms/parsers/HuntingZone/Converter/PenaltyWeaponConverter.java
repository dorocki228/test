package  l2s.Phantoms.parsers.HuntingZone.Converter;

import java.util.SortedSet;
import java.util.TreeSet;

import com.thoughtworks.xstream.converters.SingleValueConverter;

import  l2s.gameserver.templates.item.WeaponTemplate.WeaponType;

public class PenaltyWeaponConverter implements SingleValueConverter
{
	public Object fromString(String name)
	{
		SortedSet<WeaponType> penaltyWeapon = new TreeSet<WeaponType>();
		if (name!= null && !name.isBlank())
		for (String s:name.split(","))
			penaltyWeapon.add(WeaponType.valueOf(s));
		
		return penaltyWeapon;
	}
	
	@SuppressWarnings("unchecked")
	public String toString(Object name)
	{
		String penaltyWeapon = "";
		for (WeaponType tmp : ((SortedSet<WeaponType>) name))
		{
			if (penaltyWeapon.isBlank())
				penaltyWeapon= tmp.name();
			else
			penaltyWeapon= penaltyWeapon + ","+tmp.name();
		}
		return penaltyWeapon;
	}
	
	@SuppressWarnings("rawtypes")
	public boolean canConvert(Class type)
	{
		return true;// type.equals(String.class);
	}
	
}
