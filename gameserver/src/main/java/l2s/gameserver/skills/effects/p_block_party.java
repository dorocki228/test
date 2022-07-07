package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class p_block_party extends Abnormal
{
	public p_block_party(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean checkCondition()
	{
		return getEffected().isPlayer() && getTemplate().checkCondition(this);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		getEffected().getPlayer().startPartyBlock();
		getEffected().getPlayer().leaveParty();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		getEffected().getPlayer().stopPartyBlock();
	}
}
