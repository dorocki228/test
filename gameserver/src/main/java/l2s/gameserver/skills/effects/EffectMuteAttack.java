package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectMuteAttack extends Abnormal
{
	public EffectMuteAttack(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(!_effected.startAMuted())
		{
			_effected.abortCast(true, true);
			_effected.abortAttack(true, true);
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopAMuted();
	}
}
