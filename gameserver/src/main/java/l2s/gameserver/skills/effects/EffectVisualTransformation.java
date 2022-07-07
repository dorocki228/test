package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectVisualTransformation extends Abnormal
{
	public EffectVisualTransformation(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean checkCondition()
	{
		return !getEffected().isTransformImmune() && !getEffected().isInFlyingTransform() && super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		getEffected().startMuted();
		getEffected().startAMuted();
		getEffected().startPMuted();
		getEffected().abortCast(true, true);
		getEffected().abortAttack(true, true);
		getEffected().setVisualTransform((int) calc());
	}

	@Override
	public void onExit()
	{
		super.onExit();
		getEffected().setVisualTransform(null);
		getEffected().stopMuted();
		getEffected().stopAMuted();
		getEffected().stopPMuted();
	}
}
