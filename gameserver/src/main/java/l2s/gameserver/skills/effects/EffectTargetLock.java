package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectTargetLock extends Abnormal
{
	private final boolean _provokeAttack;

	public EffectTargetLock(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_provokeAttack = template.getParam().getBool("provoke_attack", false);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected != _effector)
		{
			_effected.setAggressionTarget(_effector);
			_effected.setTarget(_effector);
			_effected.getAI().clearNextAction();
			if(_provokeAttack)
				_effected.getAI().Attack(_effector, false, false);
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(_effected != _effector)
			_effected.setAggressionTarget(null);
	}
}
