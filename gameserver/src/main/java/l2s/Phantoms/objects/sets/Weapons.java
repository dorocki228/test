package  l2s.Phantoms.objects.sets;

import l2s.gameserver.templates.item.ItemGrade;

public class Weapons
{
	private int[] _class_Id;
	private ItemGrade _grade;
	private int weapon = 0;
	
	public Weapons(int[] _class_Id,ItemGrade grade,int item_id)
	{
		this._class_Id = _class_Id;
		this._grade = grade;
		this.weapon = item_id;
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
	
	public ItemGrade getGrade()
	{
		return _grade;
	}
	
	public int getWeapon()
	{
		return weapon;
	}
}
