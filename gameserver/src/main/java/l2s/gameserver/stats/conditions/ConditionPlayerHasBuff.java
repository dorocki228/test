package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.AbnormalType;

public final class ConditionPlayerHasBuff extends Condition
{
	private final AbnormalType _abnormalType;
	private final int _level;

	public ConditionPlayerHasBuff(AbnormalType abnormalType, int level)
	{
		_abnormalType = abnormalType;
		_level = level;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		Creature character = creature;
		if(character == null)
			return false;
		for(Abnormal effect : character.getAbnormalList().getEffects())
		{
			if(effect.getAbnormalType() != _abnormalType)
				continue;
			if(_level == -1)
				return true;
			if(effect.getAbnormalLvl() >= _level)
				return true;
		}
		return false;
	}
}
