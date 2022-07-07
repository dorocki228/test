package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectConsumeSoulsOverTime extends Abnormal
{
	public EffectConsumeSoulsOverTime(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
			return false;
		if(_effected.getConsumedSouls() < 0)
			return false;
		int damage = (int) calc();
		if(_effected.getConsumedSouls() < damage)
			_effected.setConsumedSouls(0, null);
		else
			_effected.setConsumedSouls(_effected.getConsumedSouls() - damage, null);
		return true;
	}
}
