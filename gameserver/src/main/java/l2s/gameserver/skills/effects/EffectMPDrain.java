package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectMPDrain extends Abnormal
{
	private final boolean _percent;

	public EffectMPDrain(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_percent = template.getParam().getBool("percent", false);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(getEffected().isDead())
			return;
		if(getEffected() == getEffector())
			return;
		double drained = calc();
		if(_percent)
			drained *= getEffected().getMaxMp() / 100.0;
		drained = Math.min(drained, getEffected().getCurrentMp());
		if(drained <= 0.0)
			return;
		getEffected().setCurrentMp(Math.max(0.0, getEffected().getCurrentMp() - drained));
		getEffected().sendPacket(new SystemMessagePacket(SystemMsg.S2S_MP_HAS_BEEN_DRAINED_BY_C1).addNumber(Math.round(drained)).addName(getEffector()));
		double newMp = getEffector().getCurrentMp() + drained;
		newMp = Math.max(0.0, Math.min(newMp, getEffector().getMaxMp() / 100.0 * getEffector().calcStat(Stats.MP_LIMIT, null, null)));
		double addToMp = newMp - getEffected().getCurrentMp();
		if(addToMp > 0.0)
			getEffector().setCurrentMp(newMp);
	}
}
