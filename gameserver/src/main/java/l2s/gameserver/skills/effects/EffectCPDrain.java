package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectCPDrain extends Abnormal
{
	private final boolean _percent;

	public EffectCPDrain(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
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
			drained *= getEffected().getMaxCp() / 100.0;
		drained = Math.min(drained, getEffected().getCurrentCp());
		if(drained <= 0.0)
			return;
		getEffected().setCurrentCp(Math.max(0.0, getEffected().getCurrentCp() - drained));
		double newCp = getEffector().getCurrentCp() + drained;
		newCp = Math.max(0.0, Math.min(newCp, getEffector().getMaxCp() / 100.0 * getEffector().calcStat(Stats.CP_LIMIT, null, null)));
		double addToCp = newCp - getEffected().getCurrentCp();
		if(addToCp > 0.0)
			getEffector().setCurrentCp(newCp);
	}
}
