package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.stats.StatModifierType;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class p_max_mp extends p_abstract_stat_effect
{
	private final boolean _heal;

	public p_max_mp(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template, Stats.MAX_MP);
		_heal = template.getParam().getBool("heal", false);
	}

	@Override
	protected void afterApplyActions()
	{
		if(!_heal || getEffected().isHealBlocked())
			return;
		double power = calc();
		if(getModifierType() == StatModifierType.PER)
			power = power / 100.0 * getEffected().getMaxMp();
		if(power > 0.0)
			getEffected().setCurrentMp(getEffected().getCurrentMp() + power, false);
	}
}
