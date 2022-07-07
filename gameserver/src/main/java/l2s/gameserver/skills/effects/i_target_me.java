package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.skill.EffectTemplate;

public class i_target_me extends i_abstract_effect
{
	public i_target_me(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(getEffected() == getEffector())
			return false;

		return super.checkCondition();
	}

	@Override
	public void instantUse()
	{
		getEffected().setTarget(getEffector());
		getEffected().abortCast(true, true);
		getEffected().abortAttack(true, true);
		getEffected().getAI().clearNextAction();
	}
}
