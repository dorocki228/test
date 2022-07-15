package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.SkillCastingType;
import l2s.gameserver.skills.SkillEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConditionPlayerCastingSkillId extends Condition
{
	private final int _skillId;

	public ConditionPlayerCastingSkillId(int skillId)
	{
		_skillId = skillId;
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		SkillEntry skillEntry = actor.getSkillCast(SkillCastingType.NORMAL).getSkillEntry();
		if(skillEntry != null && skillEntry.getId() == _skillId)
			return true;

		skillEntry = actor.getSkillCast(SkillCastingType.NORMAL_SECOND).getSkillEntry();
		if(skillEntry != null && skillEntry.getId() == _skillId)
			return true;

		return false;
	}
}