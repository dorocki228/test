package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectDamageBlock extends Abnormal
{
	private final boolean _withException;

	public EffectDamageBlock(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_withException = template.getParam().getBool("with_exception", false);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		getEffected().startDamageBlocked();
		if(_withException)
			if(getEffected() == getEffector())
				getEffected().setDamageBlockedException(getEffector().getCastingTarget());
			else
				getEffected().setDamageBlockedException(getEffector());
	}

	@Override
	public void onExit()
	{
		super.onExit();
		getEffected().stopDamageBlocked();
		getEffected().setDamageBlockedException(null);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
