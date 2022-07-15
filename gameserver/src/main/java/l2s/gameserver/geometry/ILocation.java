package l2s.gameserver.geometry;

import l2s.gameserver.utils.PositionUtils;
import l2s.gameserver.utils.PositionUtils.Position;

/**
 * @author Bonux
**/
public interface ILocation
{
	public int getX();

	public int getY();

	public int getZ();

	public int getHeading();

	default public long getXYDeltaSq(int x, int y)
	{
		long dx = x - getX();
		long dy = y - getY();
		return dx * dx + dy * dy;
	}

	default public long getXYDeltaSq(ILocation loc)
	{
		return getXYDeltaSq(loc.getX(), loc.getY());
	}

	default public long getZDeltaSq(int z)
	{
		long dz = z - getZ();
		return dz * dz;
	}

	default public long getZDeltaSq(ILocation loc)
	{
		return getZDeltaSq(loc.getZ());
	}

	default public long getXYZDeltaSq(int x, int y, int z)
	{
		return getXYDeltaSq(x, y) + getZDeltaSq(z);
	}

	default public long getXYZDeltaSq(ILocation loc)
	{
		return getXYZDeltaSq(loc.getX(), loc.getY(), loc.getZ());
	}

	default public int getDistance(int x, int y)
	{
		return (int) Math.sqrt(getXYDeltaSq(x, y));
	}

	default public int getDistance(int x, int y, int z)
	{
		return (int) Math.sqrt(getXYZDeltaSq(x, y, z));
	}

	default public int getDistance(ILocation loc)
	{
		return getDistance(loc.getX(), loc.getY());
	}

	default public int getDistance3D(ILocation loc)
	{
		return getDistance(loc.getX(), loc.getY(), loc.getZ());
	}

	default public boolean isInRangeSq(ILocation loc, long range)
	{
		return getXYDeltaSq(loc) <= range;
	}

	default public boolean isInRange(ILocation loc, int range)
	{
		return isInRangeSq(loc, (long) range * range);
	}

	default public boolean isInRangeZ(ILocation loc, int range)
	{
		return isInRangeZSq(loc, (long) range * range);
	}

	default public boolean isInRangeZSq(ILocation loc, long range)
	{
		return getXYZDeltaSq(loc) <= range;
	}

	default public long getSqDistance(int x, int y)
	{
		return getXYDeltaSq(x, y);
	}

	default public long getSqDistance(ILocation loc)
	{
		return getXYDeltaSq(loc);
	}

	/**
	 * @param to
	 * @return degree value of object 2 to the horizontal line with object 1 being the origin
	 */
	default double calculateAngleTo(ILocation to)
	{
		return PositionUtils.calculateAngleFrom(getX(), getY(), to.getX(), to.getY());
	}

	/**
	 * @param to
	 * @return the heading to the target specified
	 */
	default int calculateHeadingTo(ILocation to)
	{
		return PositionUtils.calculateHeadingFrom(getX(), getY(), to.getX(), to.getY());
	}

	/**
	 * Calculates the angle in degrees from this object to the given object.<br>
	 * The return value can be described as how much this object has to turn<br>
	 * to have the given object directly in front of it.
	 * @param target the object to which to calculate the angle
	 * @return the angle this object has to turn to have the given object in front of it
	 */
	default double calculateDirectionTo(ILocation target)
	{
		int heading = PositionUtils.calculateHeadingFrom(getX(), getY(), target.getX(), target.getY()) - getHeading();
		if (heading < 0)
		{
			heading = 65535 + heading;
		}
		return PositionUtils.convertHeadingToDegree(heading);
	}

	/**
	 * Computes the 2D Euclidean distance between this locational and (x, y).
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return the 2D Euclidean distance between this locational and (x, y)
	 */
	default double distance2d(double x, double y)
	{
		return Math.sqrt(Math.pow(getX() - x, 2) + Math.pow(getY() - y, 2));
	}

	/**
	 * Computes the 2D Euclidean distance between this locational and locational loc.
	 * @param loc the locational
	 * @return the 2D Euclidean distance between this locational and locational loc
	 */
	default double distance2d(ILocation loc)
	{
		return distance2d(loc.getX(), loc.getY());
	}

	/**
	 * Computes the 3D Euclidean distance between this locational and (x, y, z).
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return the 3D Euclidean distance between this locational and (x, y, z)
	 */
	default double distance3d(double x, double y, double z)
	{
		return Math.sqrt(Math.pow(getX() - x, 2) + Math.pow(getY() - y, 2) + Math.pow(getZ() - z, 2));
	}

	/**
	 * Computes the 3D Euclidean distance between this locational and locational loc.
	 * @param loc the locational
	 * @return the 3D Euclidean distance between this locational and locational loc
	 */
	default double distance3d(ILocation loc)
	{
		return distance3d(loc.getX(), loc.getY(), loc.getZ());
	}

	/**
	 * Checks if this locational is in 2D Euclidean radius of (x, y)
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param radius the radius
	 * @return {@code true} if this locational is in radius of (x, y), {@code false} otherwise
	 */
	default boolean isInRadius2d(double x, double y, double radius)
	{
		return distance2d(x, y) <= radius;
	}

	/**
	 * Checks if this locational is in 2D Euclidean radius of locational loc
	 * @param loc the locational
	 * @param radius the radius
	 * @return {@code true} if this locational is in radius of locational loc, {@code false} otherwise
	 */
	default boolean isInRadius2d(ILocation loc, double radius)
	{
		return isInRadius2d(loc.getX(), loc.getY(), radius);
	}

	/**
	 * Checks if this locational is in 3D Euclidean radius of (x, y, z)
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @param radius the radius
	 * @return {@code true} if this locational is in radius of (x, y, z), {@code false} otherwise
	 */
	default boolean isInRadius3d(double x, double y, double z, double radius)
	{
		return distance3d(x, y, z) <= radius;
	}

	/**
	 * Checks if this locational is in 3D Euclidean radius of locational loc
	 * @param loc the locational
	 * @param radius the radius
	 * @return {@code true} if this locational is in radius of locational loc, {@code false} otherwise
	 */
	default boolean isInRadius3d(ILocation loc, double radius)
	{
		return isInRadius3d(loc.getX(), loc.getY(), loc.getZ(), radius);
	}

	/**
	 * @param target
	 * @return {@code true} if this location is in front of the target location based on the game's concept of position.
	 */
	default boolean isInFrontOf(ILocation target)
	{
		return Position.FRONT.equals(l2s.gameserver.model.base.PositionUtils2.INSTANCE.getPosition(this, target));
	}

	/**
	 * @param target
	 * @return {@code true} if this location is in one of the sides of the target location based on the game's concept of position.
	 */
	default boolean isOnSideOf(ILocation target)
	{
		return Position.SIDE.equals(l2s.gameserver.model.base.PositionUtils2.INSTANCE.getPosition(this, target));
	}

	/**
	 * @param target
	 * @return {@code true} if this location is behind the target location based on the game's concept of position.
	 */
	default boolean isBehind(ILocation target)
	{
		return Position.BACK.equals(l2s.gameserver.model.base.PositionUtils2.INSTANCE.getPosition(this, target));
	}
}
