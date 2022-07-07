package l2s.gameserver.templates.npc;

import l2s.commons.logging.LoggerObject;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.utils.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class WalkerRoute extends LoggerObject
{
    private static final Logger logger = LogManager.getLogger(WalkerRoute.class);

	private final int _id;
	private final WalkerRouteType _type;
	private final List<WalkerRoutePoint> _points;

	public WalkerRoute(int id, WalkerRouteType type)
	{
		_points = new ArrayList<>();
		_id = id;
		_type = type;
	}

	public int getId()
	{
		return _id;
	}

	public WalkerRouteType getType()
	{
		return _type;
	}

	public void addPoint(WalkerRoutePoint route)
	{
		_points.add(route);
	}

	public WalkerRoutePoint getPoint(int id)
	{
		return _points.get(id);
	}

	public int size()
	{
		return _points.size();
	}

	public boolean isValid()
	{
		if(size() >= 2)
		{
		    var notValidPoints = new ArrayList<WalkerRoutePoint>();

			for(int i = 0; i < size() - 1; i++)
			{
				WalkerRoutePoint point = getPoint(i);
				WalkerRoutePoint nextPoint = getPoint(i + 1);

                Location pointLocation = point.getLocation();
                Location nextPointLocation = nextPoint.getLocation();

                double distance = pointLocation.distance3D(nextPointLocation);
				if(distance > 1000)
                {
                    notValidPoints.add(point);
                    logger.error("Walker route {} isn't valid: distance is too much between points {} and {}",
                            _id, pointLocation, nextPointLocation);
                }

                if(!GeoEngine.canMoveToCoord(pointLocation.getX(), pointLocation.getY(), pointLocation.getZ(),
                        nextPointLocation.getX(), nextPointLocation.getY(), nextPointLocation.getZ(), 0))
                {
                    notValidPoints.add(point);
                    logger.error("Walker route {} isn't valid: geodata check false between points {} and {}",
                            _id, pointLocation, nextPointLocation);
                }
			}

			if(!notValidPoints.isEmpty())
                return false;
		}

		if(_type == WalkerRouteType.DELETE || _type == WalkerRouteType.FINISH)
			return size() > 0;
		return size() > 1;
	}
}
