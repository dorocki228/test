package l2s.commons.geometry;

public class Circle extends AbstractShape
{
	protected final Point2D c;
	protected final int r;

	public Circle(Point2D center, int radius)
	{
		c = center;
		r = radius;
		min.x = c.x - r;
		max.x = c.x + r;
		min.y = c.y - r;
		max.y = c.y + r;
	}

	public Circle(int x, int y, int radius)
	{
		this(new Point2D(x, y), radius);
	}

	public Point2D getCenter()
	{
		return c;
	}

	public int getRadius()
	{
		return r;
	}

	@Override
	public Circle setZmax(int z)
	{
		max.z = z;
		return this;
	}

	@Override
	public Circle setZmin(int z)
	{
		min.z = z;
		return this;
	}

	@Override
	public boolean isInside(int x, int y)
	{
		return (int) StrictMath.pow(x - c.x, 2.0) + (int) StrictMath.pow(y - c.y, 2.0) <= (int) StrictMath.pow(r, 2.0);
	}

	public boolean isInside(Circle circle)
	{
		double distance = distance(circle.c);
		if(distance > (r + circle.r))
		{
			// No overlap
			return false;
		}
		else if((distance <= Math.abs(r - circle.r)))
		{
			// Inside
			return true;
		}
		else // if (distance <= r1 + r2)
		{
			// Overlap
			return false;
		}
	}

	@Override
	public boolean isOnPerimeter(int x, int y)
	{
		return Math.abs((int) StrictMath.pow(x - c.x, 2.0) + (int) StrictMath.pow(y - c.y, 2.0) - (int) StrictMath.pow(r, 2.0)) < 48;
	}

	public Double distance(Point2D point)
	{
		if(point == null)
			return null;
		return c.distance(point);
	}

	@Override
	public String toString()
	{
		return "[" +
				c + "{ radius: " + r + "}" +
				"]";
	}
}
