package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectDistortedSpace extends Abnormal
{
	public EffectDistortedSpace(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		getEffected().startDistortedSpace();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		getEffected().stopDistortedSpace();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}