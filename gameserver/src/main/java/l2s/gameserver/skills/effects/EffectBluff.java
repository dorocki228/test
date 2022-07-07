package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.s2c.FinishRotatingPacket;
import l2s.gameserver.network.l2.s2c.StartRotatingPacket;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectBluff extends Abnormal
{
	public EffectBluff(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean checkCondition()
	{
		return (!getEffected().isNpc() || getEffected().isMonster()) && super.checkCondition();
	}

	@Override
	public boolean onActionTime()
	{
		getEffected().broadcastPacket(new StartRotatingPacket(getEffected(), getEffected().getHeading(), 1, 65535));
		getEffected().broadcastPacket(new FinishRotatingPacket(getEffected(), getEffector().getHeading(), 65535));
		getEffected().setHeading(getEffector().getHeading());
		return true;
	}
}
