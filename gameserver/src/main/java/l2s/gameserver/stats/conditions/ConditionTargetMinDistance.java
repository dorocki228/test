package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionTargetMinDistance extends Condition
{
	private final int _distance;

	public ConditionTargetMinDistance(int distance)
	{
		_distance = distance;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		return target != null && !creature.isInRange(target.getLoc(), _distance);
	}
}
