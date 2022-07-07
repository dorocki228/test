package  l2s.Phantoms.objects.sets;

import l2s.gameserver.templates.item.ItemGrade;

public class UnderwearSet
{
	private ItemGrade _grade;
	
	private int shirt = 0;
	private int belt = 0;
	
	public UnderwearSet(ItemGrade grade,int shirt,int belt)
	{
		this._grade = grade;
		this.shirt = shirt;
		this.belt = belt;
	}

	public ItemGrade getGrade()
	{
		return _grade;
	}
	
	public int getShirt()
	{
		return shirt;
	}
	
	public int getBelt()
	{
		return belt;
	}
}
