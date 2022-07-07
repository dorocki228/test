package  l2s.Phantoms.objects.sets;

import l2s.gameserver.templates.item.ItemGrade;

public class JewelSet
{
	private int earring_L = 0;
	private int earring_R = 0;
	private int ring_L = 0;
	private int ring_R = 0;
	private int necklace = 0;
	
	private ItemGrade _grade;
	private int[] _class_Id;

	public JewelSet(int[] _class_Id,ItemGrade grade,int earring_left,int earring_right,int ring_left,int ring_right,int necklace)
	{
		this._class_Id = _class_Id;
		this._grade = grade;
		this.earring_L = earring_left;
		this.earring_R = earring_right;
		this.ring_L = ring_left;
		this.ring_R = ring_right;
		this.necklace = necklace;
	}

	public ItemGrade getGrade()
	{
		return _grade;
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
	
	public int getEarringL()
	{
		return earring_L;
	}
	
	public int getEarringR()
	{
		return earring_R;
	}
	
	public int getRingL()
	{
		return ring_L;
	}
	
	public int getRingR()
	{
		return ring_R;
	}
	
	public int getNecklace()
	{
		return necklace;
	}
}
