package l2s.commons.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Polygon extends AbstractShape
{
	private final Point2D[] points;

	public Polygon(List<Point2D> points, Point3D min, Point3D max)
	{
		super(min, max);
		this.points = points.toArray(new Point2D[0]);
	}

	@Override
	public Polygon setZmax(int z)
	{
		max.z = max.z == 0 ? z : Math.max(max.z, z);
		return this;
	}

	@Override
	public Polygon setZmin(int z)
	{
		min.z = min.z == 0 ? z : Math.min(min.z, z);
		return this;
	}

	@Override
	public boolean isInside(int x, int y)
	{
		if(x < min.x || x > max.x || y < min.y || y > max.y)
			return false;

		int hits = 0;
		int npoints = points.length;
		if(npoints == 0)
			return false;
		Point2D last = points[npoints - 1];

		Point2D cur;
		for(int i = 0; i < npoints; last = cur, i++)
		{
			cur = points[i];

			if(cur.y == last.y)
				continue;

			int leftx;
			if(cur.x < last.x)
			{
				if(x >= last.x)
					continue;
				leftx = cur.x;
			}
			else
			{
				if(x >= cur.x)
					continue;
				leftx = last.x;
			}

			double test1;
			double test2;
			if(cur.y < last.y)
			{
				if(y < cur.y || y >= last.y)
					continue;
				if(x < leftx)
				{
					hits++;
					continue;
				}
				test1 = x - cur.x;
				test2 = y - cur.y;
			}
			else
			{
				if(y < last.y || y >= cur.y)
					continue;
				if(x < leftx)
				{
					hits++;
					continue;
				}
				test1 = x - last.x;
				test2 = y - last.y;
			}

			if(test1 < test2 / (last.y - cur.y) * (last.x - cur.x))
				hits++;
		}

		return (hits & 1) != 0;
	}

	@Override
	public boolean isOnPerimeter(int x, int y)
	{
		return isInside(x, y);
	}

	@Override
	public String toString()
	{
		return Arrays.stream(points)
				.map(Point2D::toString)
				.collect(Collectors.joining(",", "[", "]"));
	}

	public static class PolygonBuilder
	{
		private final List<Point2D> points = new ArrayList<>(4);

		private final Point3D max = new Point3D();
		private final Point3D min = new Point3D();

		public PolygonBuilder add(int x, int y)
		{
			add(new Point2D(x, y));
			return this;
		}

		public PolygonBuilder add(Point2D p)
		{
			if(points.isEmpty())
			{
				min.y = p.y;
				min.x = p.x;
				max.x = p.x;
				max.y = p.y;
			}
			else
			{
				min.y = Math.min(min.y, p.y);
				min.x = Math.min(min.x, p.x);
				max.x = Math.max(max.x, p.x);
				max.y = Math.max(max.y, p.y);
			}

			points.add(p);

			return this;
		}

		public PolygonBuilder setZmax(int z)
		{
			max.z = max.z == 0 ? z : Math.max(max.z, z);

			return this;
		}

		public PolygonBuilder setZmin(int z)
		{
			min.z = min.z == 0 ? z : Math.min(min.z, z);

			return this;
		}

		public Polygon createPolygon()
		{
			return new Polygon(points, min, max);
		}

		public boolean validate()
		{
			if(points.size() < 3)
				return false;
			if(points.size() > 3)
				for(int i = 1; i < points.size(); ++i)
				{
					int ii = i + 1 < points.size() ? i + 1 : 0;
					for(int n = i; n < points.size(); ++n)
						if(Math.abs(n - i) > 1)
						{
							int nn = n + 1 < points.size() ? n + 1 : 0;
							if(GeometryUtils.checkIfLineSegementsIntersects(points.get(i), points.get(ii),
									points.get(n), points.get(nn)))
								return false;
						}
				}
			return true;
		}

		@Override
		public String toString()
		{
			return points.stream()
					.map(Point2D::toString)
					.collect(Collectors.joining(",", "[", "]"));
		}
	}
}
