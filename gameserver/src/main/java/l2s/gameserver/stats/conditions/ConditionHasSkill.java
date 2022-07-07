package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

public final class ConditionHasSkill extends Condition
{
	private final Integer _id;
	private final int _level;

	public ConditionHasSkill(Integer id, int level)
	{
		_id = id;
		_level = level;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		return creature.getSkillLevel(_id) >= _level;
	}
}
