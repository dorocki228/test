package  l2s.Phantoms.objects;

import l2s.gameserver.templates.item.ItemGrade;

public class GveZoneParam
{
	private int minLvl;
	private int maxLvl;
	private ItemGrade maxGrade;
	
	public int getMinLvl() {
		return minLvl;
	}

	public void setMinLvl(int minLvl) {
		this.minLvl = minLvl;
	}

	public int getMaxLvl() {
		return maxLvl;
	}

	public void setMaxLvl(int maxLvl) {
		this.maxLvl = maxLvl;
	}

	public ItemGrade getMaxGrade() {
		return maxGrade;
	}

	public void setMaxGrade(ItemGrade maxGrade) {
		this.maxGrade = maxGrade;
	}

	public GveZoneParam()
	{
	}
	
}