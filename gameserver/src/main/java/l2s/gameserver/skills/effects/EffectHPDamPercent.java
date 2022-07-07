package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectHPDamPercent extends Abnormal
{
	public EffectHPDamPercent(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected.isDead())
			return;
		double newHp = (100.0 - calc()) * _effected.getMaxHp() / 100.0;
		newHp = Math.min(_effected.getCurrentHp(), Math.max(0.0, newHp));
		_effected.setCurrentHp(newHp, false);
	}
}
