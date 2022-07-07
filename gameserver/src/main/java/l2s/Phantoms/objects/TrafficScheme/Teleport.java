package  l2s.Phantoms.objects.TrafficScheme;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import  l2s.gameserver.utils.Location;

@XStreamAlias("teleport")
public class Teleport
{
	@XStreamAlias("x")
	@XStreamAsAttribute
	private int x;
	
	@XStreamAlias("y")	
	@XStreamAsAttribute
	private int y;
	
	@XStreamAlias("z")
	@XStreamAsAttribute
	private int z;
	
	public Teleport()
	{}
	
	public Teleport(int tx, int ty, int tz)
	{
		this.x = tx;
		this.y = ty;
		this.z = tz;
	}
	
	public Teleport(Location loc)
	{
		this.x = loc.getX();
		this.y = loc.getY();
		this.z = loc.getZ();
	}

	public Location getLoc()
	{
		return new Location(getX(),getY(),getZ());
	}
	
	public int getX()
	{
		return x;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public int getY()
	{
		return y;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	public int getZ()
	{
		return z;
	}

	public void setZ(int z)
	{
		this.z = z;
	}
	
  @Override
  public String toString() 
  {
      return " x:" +getX() + " y:" + getY() + " z:" +getZ();
  }
}