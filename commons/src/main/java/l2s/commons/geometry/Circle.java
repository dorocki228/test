package l2s.commons.geometry;

import java.util.ArrayList;
import java.util.List;

public class Circle extends AbstractShape
{
	protected final Point2D[] points;
	protected final Point2D center;
	protected final int r;

	public Circle(Point2D center, int radius)
	{
		this.center = center;
		r = radius;
		min.x = (center.x - r);
		max.x = (center.x + r);
		min.y = (center.y - r);
		max.y = (center.y + r);

		List<Point2D> points = new ArrayList<Point2D>();
		for(int deegre = 0; deegre <= 360; deegre += 36)
		{
			double radians = Math.toRadians(deegre);
			int x = (int) (center.getX() - r * Math.sin(radians));
			int y = (int) (center.getY() + r * Math.cos(radians));
			points.add(new Point2D(x, y));
		}
		this.points = points.toArray(new Point2D[points.size()]);
	}

	public Circle(int x, int y, int radius)
	{
		this(new Point2D(x, y), radius);
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
	public boolean isInside(int x, int y, CoordsConverter c)
	{
		return (int) Math.pow(x - c.convertX(center.x), 2) + (int) Math.pow(y - c.convertY(center.y), 2) <= (int) Math.pow(c.convertDistance(r), 2);
	}

	@Override
	public boolean isOnPerimeter(int x, int y, CoordsConverter c)
	{
		return (int) Math.pow(x - c.convertX(center.x), 2) + (int) Math.pow(y - c.convertY(center.y), 2) == (int) Math.pow(c.convertDistance(r), 2);
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(center).append("{ radius: ").append(r).append("}");
		sb.append("]");
		return sb.toString();
	}

	@Override
	public Point2D getCenter()
	{
		return center;
	}

	@Override
	public Point2D getNearestPoint(int x, int y)
	{
		return GeometryUtils.getNearestPointOnCircle(center, r, x, y);
	}

	@Override
	public int getRadius()
	{
		return r;
	}

	@Override
	public Point2D[] getPoints()
	{
		return points;
	}
}
