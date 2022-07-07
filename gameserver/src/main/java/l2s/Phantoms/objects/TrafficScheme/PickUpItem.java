package  l2s.Phantoms.objects.TrafficScheme;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("pick_up_item")
public class PickUpItem
{
	@XStreamAlias("id")
	@XStreamAsAttribute
	private int _id;
	
	
	public PickUpItem()
	{}
	
	public PickUpItem(int item_id)
	{
		this.setId(item_id);
	}
	
	public int getId()
	{
		return _id;
	}

	public void setId(int id)
	{
		this._id = id;
	}


}