package l2s.gameserver.skills.effects;

import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectFlyUp extends Abnormal
{
	public EffectFlyUp(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean checkCondition()
	{
		return !getEffected().isPeaceNpc() && super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		getEffected().startFlyUp();
		getEffected().abortAttack(true, true);
		getEffected().abortCast(true, true);
		getEffected().stopMove();
		getEffected().getAI().notifyEvent(CtrlEvent.EVT_FLY_UP, getEffected());
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(getEffected().isFlyUp())
		{
			getEffected().stopFlyUp();
			if(!getEffected().isPlayer())
				getEffected().getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
	}
}
