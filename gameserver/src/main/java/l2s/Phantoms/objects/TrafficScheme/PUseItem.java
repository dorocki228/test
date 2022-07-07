package  l2s.Phantoms.objects.TrafficScheme;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("use_item")
public class PUseItem
{
	@XStreamAlias("item_id")
	@XStreamAsAttribute
	private int item_id;
	
	@XStreamAlias("delay_ms")
	@XStreamAsAttribute
	private int delay;
	
	@XStreamAlias("target_id")
	@XStreamAsAttribute
	private int target_id;
	
	public PUseItem()
	{}
	
	public PUseItem(int item_id)
	{
		this.setItemId(item_id);
	}
	
	public int getItemId()
	{
		return item_id;
	}

	public void setItemId(int item_id)
	{
		this.item_id = item_id;
	}
	
  @Override
  public String toString() 
  {
      return " item_id:"+ item_id+ " delay:"+ delay +" target_id:" + target_id;
  }

	public int getItemDelay()
	{
		return delay;
	}

	public void setItemDelay(int delay)
	{
		this.delay = delay;
	}

	public int getTargetId()
	{
		return target_id;
	}

	public void setTargetId(int target_id)
	{
		this.target_id = target_id;
	}
}