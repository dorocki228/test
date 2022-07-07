package l2s.gameserver.skills.effects;

import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectHate extends Abnormal
{
	public EffectHate(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean onActionTime()
	{
		if(getEffected().isNpc() && getEffected().isMonster())
			getEffected().getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getEffector(), calc());
		return true;
	}
}
