package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Sex;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionPlayerSex extends Condition
{
	private final Sex _sex;

	public ConditionPlayerSex(Sex sex)
	{
		_sex = sex;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		return creature.isPlayer() && creature.getSex() == _sex;
	}
}
