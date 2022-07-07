package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionPlayerCastingSkillId extends Condition
{
	private final int _skillId;

	public ConditionPlayerCastingSkillId(int skillId)
	{
		_skillId = skillId;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		return creature.getCastingSkill() != null && creature.getCastingSkill().getId() == _skillId;
	}
}
