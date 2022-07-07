package l2s.commons.geometry;

public interface Shape
{
	boolean isInside(int p0, int p1);

	boolean isInside(int p0, int p1, int p2);

	boolean isOnPerimeter(int p0, int p1);

	boolean isOnPerimeter(int p0, int p1, int p2);

	int getXmax();

	int getXmin();

	int getYmax();

	int getYmin();

	int getZmax();

	int getZmin();
}
