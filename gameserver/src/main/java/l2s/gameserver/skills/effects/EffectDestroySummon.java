package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectDestroySummon extends Abnormal
{
	public EffectDestroySummon(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean checkCondition()
	{
		return _effected.isSummon() && super.checkCondition();
	}

	@Override
	public boolean onActionTime()
	{
		((Servitor) _effected).unSummon(false);
		return true;
	}
}
