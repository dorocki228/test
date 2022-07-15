package l2s.commons.geometry;

public interface Shape
{
	default boolean isInside(int x, int y) {
		return isInside(x, y, CoordsConverter.DEFAULT_CONVERTER);
	}

	boolean isInside(int x, int y, CoordsConverter converter);

	default boolean isInside(int x, int y, int z) {
		return isInside(x, y, z, CoordsConverter.DEFAULT_CONVERTER);
	}

	boolean isInside(int x, int y, int z, CoordsConverter converter);

	default boolean isOnPerimeter(int x, int y) {
		return isOnPerimeter(x, y, CoordsConverter.DEFAULT_CONVERTER);
	}

	boolean isOnPerimeter(int x, int y, CoordsConverter converter);

	default boolean isOnPerimeter(int x, int y, int z) {
		return isOnPerimeter(x, y, z, CoordsConverter.DEFAULT_CONVERTER);
	}

	boolean isOnPerimeter(int x, int y, int z, CoordsConverter converter);

	int getXmax();

	int getXmin();

	int getYmax();

	int getYmin();

	int getZmax();

	int getZmin();

	Point2D getCenter();

	Point2D getNearestPoint(int x, int y);

	int getRadius();

	Point2D[] getPoints();
}
