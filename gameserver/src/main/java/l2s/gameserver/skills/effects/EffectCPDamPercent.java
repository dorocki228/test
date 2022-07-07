package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectCPDamPercent extends Abnormal
{
	public EffectCPDamPercent(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected.isDead() || !_effected.isPlayer())
			return;
		double newCp = (100.0 - calc()) * _effected.getMaxCp() / 100.0;
		newCp = Math.min(_effected.getCurrentCp(), Math.max(0.0, newCp));
		_effected.setCurrentCp(newCp);
	}
}
