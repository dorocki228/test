package l2s.commons.geometry;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Point2D implements Cloneable
{
	public static final Point2D[] EMPTY_ARRAY = new Point2D[0];
	@XStreamAlias("x")
	@XStreamAsAttribute
	public int x;
	@XStreamAlias("y")
	@XStreamAsAttribute
	public int y;

	public Point2D()
	{}

	public Point2D(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	@Override
	public Point2D clone()
	{
		return new Point2D(x, y);
	}

	@Override
	public boolean equals(Object o)
	{
		return o == this || o != null && o.getClass() == getClass() && equals((Point2D) o);
	}

	@Override
	public int hashCode()
	{
		int hash = x;
		hash = 43 * hash + y;
		return hash;
	}

	public boolean equals(Point2D p)
	{
		return equals(p.x, p.y);
	}

	public boolean equals(int x, int y)
	{
		return this.x == x && this.y == y;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public double distance(Point2D point2D)
	{
		return distance(x, y, point2D.getX(), point2D.getY());
	}

	public double distance(double x1, double y1, double x2, double y2)
	{
		return Math.sqrt(StrictMath.pow(x1 - x2, 2) + StrictMath.pow(y1 - y2, 2));
	}

	@Override
	public String toString()
	{
		return "[x: " + x + " y: " + y + "]";
	}
}
