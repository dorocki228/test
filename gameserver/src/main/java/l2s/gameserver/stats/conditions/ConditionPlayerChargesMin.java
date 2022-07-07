package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionPlayerChargesMin extends Condition
{
	private final int _minCharges;

	public ConditionPlayerChargesMin(int minCharges)
	{
		_minCharges = minCharges;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		if(creature == null || !creature.isPlayer())
			return false;

		return creature.getIncreasedForce() >= _minCharges;
	}
}