package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectMute extends Abnormal
{
	public EffectMute(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(!_effected.startMuted())
		{
			Skill castingSkill = _effected.getCastingSkill();
			if(castingSkill != null && castingSkill.isMagic())
				_effected.abortCast(true, true);
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopMuted();
	}
}
