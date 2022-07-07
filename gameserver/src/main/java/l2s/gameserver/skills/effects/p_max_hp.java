package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.stats.StatModifierType;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class p_max_hp extends p_abstract_stat_effect
{
	private final boolean _restore;

	public p_max_hp(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template, Stats.MAX_HP);
		_restore = template.getParam().getBool("restore", false);
	}

	@Override
	protected void afterApplyActions()
	{
		if(!_restore || getEffected().isHealBlocked())
			return;
		double power = calc();
		if(getModifierType() == StatModifierType.PER)
			power = power / 100.0 * getEffected().getMaxHp();
		if(power > 0.0)
			getEffected().setCurrentHp(getEffected().getCurrentHp() + power, false);
	}
}
