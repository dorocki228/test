package l2s.gameserver.templates;

import l2s.commons.geometry.Polygon;
import l2s.commons.geometry.Polygon.PolygonBuilder;
import l2s.gameserver.utils.Location;

/**
 * @author Laky
 */
public class FenceTemplate extends CreatureTemplate
{
	private final boolean targetable;
	private final Polygon polygon;
	private final Location loc;

	public FenceTemplate(Location loc, int width, int height)
	{
		super(CreatureTemplate.getEmptyStatsSet());
		targetable = false;
		this.loc = loc;
        PolygonBuilder polygonBuilder = new PolygonBuilder(); // <- TODO
		int x0 = loc.getX();
		int y0 = loc.getY();
        polygonBuilder.add(x0 - width / 2, y0 - height / 2);
        polygonBuilder.add(x0 + width / 2, y0 + height / 2);
        polygonBuilder.add(x0 - width / 2, y0 - height / 2);
        polygonBuilder.add(x0 + width / 2, y0 + height / 2);
        polygonBuilder.setZmin(loc.getZ());
        polygonBuilder.setZmax(loc.getZ());

        polygon = polygonBuilder.createPolygon();
	}

	public Polygon getPolygon()
	{
		return polygon;
	}

	public Location getLoc()
	{
		return loc;
	}

	public boolean isTargetable()
	{
		return targetable;
	}
}
