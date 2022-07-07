package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public abstract class i_abstract_effect extends Abnormal
{
	public i_abstract_effect(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public abstract void instantUse();

	@Override
	protected final void onStart()
	{}

	@Override
	protected final boolean onActionTime()
	{
		return true;
	}

	@Override
	protected final void onExit()
	{}
}
