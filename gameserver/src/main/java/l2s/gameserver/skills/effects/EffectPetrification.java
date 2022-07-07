package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectPetrification extends Abnormal
{
	public EffectPetrification(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean checkCondition()
	{
		return !getEffected().isParalyzeImmune() && super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		getEffected().startParalyzed();
		getEffected().startDebuffImmunity();
		getEffected().startBuffImmunity();
		if(getEffected() != getEffector())
		{
			getEffected().abortAttack(true, true);
			getEffected().abortCast(true, true);
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();
		getEffected().stopParalyzed();
		getEffected().stopDebuffImmunity();
		getEffected().stopBuffImmunity();
	}
}
