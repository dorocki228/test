package  l2s.Phantoms.objects.TrafficScheme;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("equip_item")
public class EquipItem
{
	@XStreamAlias("item_id")
	@XStreamAsAttribute
	private int item_id;
	
	
	public EquipItem()
	{}
	
	public EquipItem(int item_id)
	{
		this.setId(item_id);
	}
	
	public int getId()
	{
		return item_id;
	}

	public void setId(int id)
	{
		this.item_id = id;
	}


}