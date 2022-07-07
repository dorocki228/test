package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionPlayerPercentHp extends Condition
{
	private final double _hp;

	public ConditionPlayerPercentHp(int hp)
	{
		_hp = hp / 100.0;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		return creature.getCurrentHpRatio() <= _hp;
	}
}
