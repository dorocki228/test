package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionLogicOr extends Condition
{
	private static final Condition[] emptyConditions = new Condition[0];
	public Condition[] _conditions;

	public ConditionLogicOr()
	{
		_conditions = emptyConditions;
	}

	public void add(Condition condition)
	{
		if(condition == null)
			return;

		int len = _conditions.length;
		Condition[] tmp = new Condition[len + 1];
		System.arraycopy(_conditions, 0, tmp, 0, len);
		tmp[len] = condition;
		_conditions = tmp;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		for(Condition c : _conditions)
			if(c.test(creature, target, skill, item, value))
				return true;
		return false;
	}
}
