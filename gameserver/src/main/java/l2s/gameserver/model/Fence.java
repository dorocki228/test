package l2s.gameserver.model;

import l2s.commons.geometry.Shape;
import l2s.gameserver.Config;
import l2s.gameserver.geodata.GeoCollision;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExColosseumFenceInfoPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.templates.FenceTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.utils.Location;

import java.util.Collections;
import java.util.List;

/**
 * @author Laky
 */
public class Fence extends Creature implements GeoCollision
{
	private int type;
	private final int width;
	private final int height;

	private byte[][] geoAround;

	public Fence(int objectId, Location loc, int type, int width, int height)
	{
		super(objectId, new FenceTemplate(loc, width, height));
		this.type = type;
		this.width = width;
		this.height = height;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}

	@Override
	public int getLevel()
	{
		return 0;
	}

	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getActiveWeaponTemplate()
	{
		return null;
	}

	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getSecondaryWeaponTemplate()
	{
		return null;
	}

	@Override
	public void broadcastCharInfo()
	{
		broadcastPacket(new ExColosseumFenceInfoPacket(this));
	}

	@Override
	public void validateLocation(int broadcast)
	{
	}

	@Override
	public void sendChanges()
	{
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		return Collections.singletonList(new ExColosseumFenceInfoPacket(this));
	}

	@Override
	public FenceTemplate getTemplate()
	{
		return (FenceTemplate) super.getTemplate();
	}

	@Override
	public Shape getShape()
	{
		return getTemplate().getPolygon();
	}

	@Override
	public byte[][] getGeoAround()
	{
		return geoAround;
	}

	@Override
	public void setGeoAround(byte[][] geo)
	{
		geoAround = geo;
	}

	@Override
	public boolean isConcrete()
	{
		return true;
	}

	@Override
	public boolean isMovementDisabled()
	{
		return true;
	}

	@Override
	public boolean isActionsDisabled(boolean withCast)
	{
		return true;
	}

	public void setCollision(boolean m)
	{
		if(Config.ALLOW_GEODATA)
		{
			if(m)
			{
				GeoEngine.applyGeoCollision(this, getGeoIndex());
			}
			else
			{
				GeoEngine.removeGeoCollision(this, getGeoIndex());
			}
		}
	}
}
