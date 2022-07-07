package  l2s.Phantoms.objects.sets;

import l2s.gameserver.templates.item.ItemGrade;

public class ArmorSet
{
	private int helm = 0;
	private int chest = 0;
	private int gaiter = 0;
	private int gloves = 0;
	private int boots = 0;
	private int shield = 0;
	private ItemGrade grade;
	
	private int[] class_Id;

	public ArmorSet(int[] _class_id,ItemGrade _grade,int _helm,int _chest,int _gaiter,int _gloves,int _boots,int _shield)
	{
		class_Id = _class_id;
		grade = _grade;
		helm = _helm;
		chest = _chest;
		gaiter = _gaiter;
		gloves = _gloves;
		boots = _boots;
		shield = _shield;
	}

	public int[] getAllClassId()
	{
		return class_Id;
	}
	
	public ItemGrade getGrade()
	{
		return grade;
	}
	
	public boolean getClassId(int id)
	{
		for(int classId : class_Id)
		{
			if (classId == id)
				return true;
		}
		return false;
	}
	
	public int getHelm()
	{
		return helm;
	}
	
	public int getChest()
	{
		return chest;
	}
	
	public int getGaiter()
	{
		return gaiter;
	}
	
	public int getGloves()
	{
		return gloves;
	}
	
	public int getBoots()
	{
		return boots;
	}
	
	public int getShield()
	{
		return shield;
	}
}
