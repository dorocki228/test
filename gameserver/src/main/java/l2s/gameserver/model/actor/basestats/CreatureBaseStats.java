package l2s.gameserver.model.actor.basestats;

import l2s.gameserver.model.Creature;

/**
 * @author Bonux
**/
public class CreatureBaseStats
{
	protected final Creature _owner;

	public CreatureBaseStats(Creature owner)
	{
		_owner = owner;
	}

	public Creature getOwner()
	{
		return _owner;
	}

	public double getCollisionRadius()
	{
		if(getOwner().isVisualTransformed() && getOwner().getVisualTransform().getCollisionRadius() > 0)
			return getOwner().getVisualTransform().getCollisionRadius();
		return getOwner().getTemplate().getCollisionRadius();
	}

	public double getCollisionHeight()
	{
		if(getOwner().isVisualTransformed() && getOwner().getVisualTransform().getCollisionHeight() > 0)
			return getOwner().getVisualTransform().getCollisionHeight();
		return getOwner().getTemplate().getCollisionHeight();
	}
}