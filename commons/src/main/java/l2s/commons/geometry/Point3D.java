package l2s.commons.geometry;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Point3D extends Point2D
{
	public static final Point3D[] EMPTY_ARRAY;	
	@XStreamAlias("z")
	@XStreamAsAttribute
	public int z;

	public Point3D()
	{}

	public Point3D(int x, int y, int z)
	{
		super(x, y);
		this.z = z;
	}

	public int getZ()
	{
		return z;
	}

	@Override
	public Point3D clone()
	{
		return new Point3D(x, y, z);
	}

	@Override
	public boolean equals(Object o)
	{
		return o == this || o != null && o.getClass() == getClass() && equals((Point3D) o);
	}

	@Override
	public int hashCode()
	{
		int hash = super.hashCode();
		hash = 33 * hash + z;
		return hash;
	}

	public boolean equals(Point3D p)
	{
		return equals(p.x, p.y, p.z);
	}

	public boolean equals(int x, int y, int z)
	{
		return this.x == x && this.y == y && this.z == z;
	}

	@Override
	public String toString()
	{
		return "[x: " + x + " y: " + y + " z: " + z + "]";
	}

	static
	{
		EMPTY_ARRAY = new Point3D[0];
	}
}
