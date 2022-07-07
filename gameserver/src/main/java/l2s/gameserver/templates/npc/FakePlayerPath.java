package l2s.gameserver.templates.npc;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import l2s.commons.geometry.Circle;
import l2s.gameserver.model.Territory;
import l2s.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FakePlayerPath
{
	private static final Logger _log;
	private final int _id;
	private final Location _loc;
	private final TIntSet _availNextPathes;
	private final TIntObjectMap<Point> _points;

	public FakePlayerPath(int id, Location loc, TIntSet availNextPathes)
	{
		_points = new TIntObjectHashMap();
		_id = id;
		_loc = loc;
		_availNextPathes = availNextPathes;
	}

	public int getId()
	{
		return _id;
	}

	public Location getLocation()
	{
		return _loc;
	}

	public boolean isAvailNextPath(int id)
	{
		return id != _id && _availNextPathes.contains(id);
	}

	public int[] getAvailNextPathes()
	{
		return _availNextPathes.toArray();
	}

	public Point getPoint(int id)
	{
		return _points.get(id);
	}

	public Point[] getPoints()
	{
		return _points.values(new Point[_points.size()]);
	}

	public void addPoint(Point point)
	{
		_points.put(point.getId(), point);
	}

	static
	{
		_log = LoggerFactory.getLogger(FakePlayerPath.class);
	}

	public static class Point
	{
		private final int _id;
		private final Territory _territory;
		private final int _nextPointId;
		private final int _minDelay;
		private final int _maxDelay;
		private final boolean _sitting;

		public Point(int id, Location loc, int nextPointId, int minDelay, int maxDealy, boolean sitting, int offset)
		{
			_territory = new Territory();
			_id = id;
			_territory.add(new Circle(loc.getX(), loc.getY(), offset).setZmin(loc.getZ() - 50).setZmax(loc.getZ() + 50));
			_nextPointId = nextPointId;
			_minDelay = minDelay;
			_maxDelay = maxDealy;
			_sitting = sitting;
		}

		public int getId()
		{
			return _id;
		}

		public Territory getTerritory()
		{
			return _territory;
		}

		public int getNextPointId()
		{
			return _nextPointId;
		}

		public int getMinDelay()
		{
			return _minDelay;
		}

		public int getMaxDelay()
		{
			return _maxDelay;
		}

		public boolean sitting()
		{
			return _sitting;
		}
	}
}
