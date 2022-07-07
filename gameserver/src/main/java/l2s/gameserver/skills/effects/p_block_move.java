package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class p_block_move extends Abnormal
{
	public p_block_move(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if(!getEffected().isMoveBlocked())
		{
			getEffected().startMoveBlock();
			getEffected().stopMove();
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();

		if(getEffected().isMoveBlocked())
			getEffected().stopMoveBlock();
	}
}
