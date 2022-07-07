package  l2s.Phantoms.objects.TrafficScheme;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("kill_npc_id")
public class KillNpcId
{
	@XStreamAlias("id")
	@XStreamAsAttribute
	private int id;
	
	@XStreamAlias("radius")
	@XStreamAsAttribute
	private int radius;
	
	@XStreamAlias("height")
	@XStreamAsAttribute
	private int height;
	
	public KillNpcId()
	{}
	
	public KillNpcId(int item_id, int radius, int height)
	{
		this.setId(item_id);
		this.setRadius(radius);
		this.setHeight(height);
	}
	
	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public int getRadius()
	{
		return radius;
	}

	public void setRadius(int radius)
	{
		this.radius = radius;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}




}