package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionPlayerFlagged extends Condition
{
	private final boolean _flagged;

	public ConditionPlayerFlagged(boolean flagged)
	{
		_flagged = flagged;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		if(_flagged)
			return creature.getPvpFlag() > 0;
		return creature.getPvpFlag() <= 0;
	}
}
