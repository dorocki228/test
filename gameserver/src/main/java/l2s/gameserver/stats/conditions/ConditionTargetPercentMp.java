package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionTargetPercentMp extends Condition
{
	private final double _mp;

	public ConditionTargetPercentMp(int mp)
	{
		_mp = mp / 100.0;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		return target != null && target.getCurrentMpRatio() <= _mp;
	}
}
