package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionThisLevel extends Condition
{
	private final int _level;

	public ConditionThisLevel(int level)
	{
		_level = level;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		return skill != null && skill.getLevel() == _level;
	}
}
