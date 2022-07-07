package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionPlayerIsClanLeader extends Condition
{
	private final boolean _value;

	public ConditionPlayerIsClanLeader(boolean value)
	{
		_value = value;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		if(!creature.isPlayer())
			return !_value;
		return creature.getPlayer().isClanLeader() == _value;
	}
}
