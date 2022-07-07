package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionPlayerCanUntransform extends Condition
{
	private final boolean _val;

	public ConditionPlayerCanUntransform(boolean val)
	{
		_val = val;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		if(!creature.isPlayer())
			return !_val;
		Player player = creature.getPlayer();
		if(!player.isTransformed())
			return !_val;
		if(player.isInFlyingTransform() && Math.abs(player.getZ() - player.getLoc().correctGeoZ().z) > 333)
			return !_val;
		return _val;
	}
}
