package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectHPDrain extends Abnormal
{
	private final boolean _percent;

	public EffectHPDrain(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
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
			drained *= getEffected().getMaxHp() / 100.0;
		drained = Math.min(drained, getEffected().getCurrentHp());
		if(drained <= 0.0)
			return;
		getEffected().setCurrentHp(Math.max(0.0, getEffected().getCurrentHp() - drained), false);
		getEffected().sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_DRAINED_YOU_OF_S2_HP).addName(getEffector()).addNumber(Math.round(drained)));
		double newHp = getEffector().getCurrentHp() + drained;
		newHp = Math.max(0.0, Math.min(newHp, getEffector().getMaxHp() / 100.0 * getEffector().calcStat(Stats.HP_LIMIT, null, null)));
		double addToHp = newHp - getEffected().getCurrentHp();
		if(addToHp > 0.0)
			getEffector().setCurrentHp(newHp, false);
	}
}
