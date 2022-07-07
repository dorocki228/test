package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.items.ItemInstance;

public final class ConditionTargetHasBuffId extends Condition
{
	private final int _id;
	private final int _level;

	public ConditionTargetHasBuffId(int id, int level)
	{
		_id = id;
		_level = level;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		if(target == null)
			return false;
		for(Abnormal effect : target.getAbnormalList().getEffects())
		{
			if(effect.getSkill().getId() != _id)
				continue;
			if(_level == -1)
				return true;
			if(effect.getSkill().getLevel() >= _level)
				return true;
		}
		return false;
	}
}
