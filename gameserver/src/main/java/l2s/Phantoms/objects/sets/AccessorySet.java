package  l2s.Phantoms.objects.sets;


import java.util.Arrays;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.Fraction;
import  l2s.gameserver.templates.StatsSet;

public class AccessorySet
{
	private int male_hat = 0;
	private int female_hat = 0;
	private int[] cloak;
	private int[] _class_Id;
	private int _cloak_chance=0;
	
	public AccessorySet(StatsSet set)
	{
		_class_Id = Arrays.stream(set.getString("class_Id").split(";")).mapToInt(Integer::parseInt).toArray();
		male_hat = set.getInteger("male_hat", 0);
		female_hat = set.getInteger("female_hat", 0);
		cloak = Arrays.stream(set.getString("cloak").split(";")).mapToInt(Integer::parseInt).toArray();
		_cloak_chance = set.getInteger("cloak_chance", 0);
	}
	
	public boolean getClassId(int id)
	{
		for(int classId : _class_Id)
		{
			if (classId == id)
				return true;
		}
		return false;
	}
	
	public int getAccessory(boolean isMale)
	{
		int acc = isMale ? male_hat : female_hat;
		if (acc == 0)
			return 0;
		return 0;
	}
	
	public int getCloak(Fraction fraction)
	{
		if(_cloak_chance>0&&Rnd.chance(_cloak_chance))
			return 0;
			
		if (cloak.length==0)
			return 0;
		if (cloak.length==1)
			return cloak[0];
		
		switch(fraction)
		{
		case FIRE:
			return cloak[0];
		case WATER:
			return cloak[1];
		default:
			return cloak[0];
		}
	}
}
