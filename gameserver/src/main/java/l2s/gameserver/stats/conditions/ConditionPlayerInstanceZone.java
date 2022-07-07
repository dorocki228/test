package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionPlayerInstanceZone extends Condition
{
	private final int _id;

	public ConditionPlayerInstanceZone(int id)
	{
		_id = id;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		Reflection ref = creature.getReflection();
		return ref.getInstancedZoneId() == _id;
	}
}