package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.geometry.Circle;
import l2s.commons.geometry.Shape;
import l2s.gameserver.geodata.GeoCollision;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.templates.npc.NpcTemplate;

public class SymbolWithGeodataInstance extends SymbolInstance implements GeoCollision
{
	private final int _geoRadius;
	private final int _geoHeight;
	private Shape _shape;
	private byte[][] _geoAround;

	public SymbolWithGeodataInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
		_geoRadius = getParameter("geodata_radius", (int) getColRadius());
		_geoHeight = getParameter("geodata_height", (int) getColHeight());
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		Circle circle = new Circle(getLoc().x, getLoc().y, _geoRadius);
		circle.setZmin(getLoc().z - 50);
		circle.setZmax(getLoc().z + _geoHeight);
		_shape = circle;
		GeoEngine.applyGeoCollision(this, getGeoIndex());
	}

	@Override
	protected void onDespawn()
	{
		super.onDespawn();
		GeoEngine.removeGeoCollision(this, getGeoIndex());
	}

	@Override
	public Shape getShape()
	{
		return _shape;
	}

	@Override
	public byte[][] getGeoAround()
	{
		return _geoAround;
	}

	@Override
	public void setGeoAround(byte[][] geo)
	{
		_geoAround = geo;
	}

	@Override
	public boolean isConcrete()
	{
		return false;
	}
}
