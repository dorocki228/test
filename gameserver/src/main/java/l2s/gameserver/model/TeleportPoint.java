package l2s.gameserver.model;

import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.utils.Location;

public class TeleportPoint
{
	private Location _loc;
	private Reflection _reflection;

	public TeleportPoint(Location loc, Reflection reflection)
	{
		_reflection = ReflectionManager.MAIN;
		_loc = loc;
		_reflection = reflection;
	}

	public TeleportPoint(Location loc)
	{
		_reflection = ReflectionManager.MAIN;
		_loc = loc;
	}

	public TeleportPoint()
	{
		_reflection = ReflectionManager.MAIN;
	}

	public Location getLoc()
	{
		return _loc;
	}

	public TeleportPoint setLoc(Location loc)
	{
		_loc = loc;
		return this;
	}

	public Reflection getReflection()
	{
		return _reflection;
	}

	public TeleportPoint setReflection(Reflection reflection)
	{
		_reflection = reflection;
		return this;
	}
}
