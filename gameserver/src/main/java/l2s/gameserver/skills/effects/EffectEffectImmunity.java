package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectEffectImmunity extends Abnormal
{
	private final boolean _withException;

	public EffectEffectImmunity(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_withException = template.getParam().getBool("with_exception", false);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		getEffected().startEffectImmunity();
		if(_withException)
			if(getEffected() == getEffector())
				getEffected().setEffectImmunityException(getEffector().getCastingTarget());
			else
				getEffected().setEffectImmunityException(getEffector());
	}

	@Override
	public void onExit()
	{
		super.onExit();
		getEffected().stopEffectImmunity();
		getEffected().setEffectImmunityException(null);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
