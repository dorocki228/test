package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.templates.skill.EffectTemplate;

public class i_m_attack extends Abnormal
{
	public i_m_attack(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean onActionTime()
	{
		if(getEffected().isDead())
			return false;
		Creature realTarget = isReflected() ? getEffector() : getEffected();
		Formulas.AttackInfo info = Formulas.calcMagicDam(getEffector(), realTarget, getSkill(), calc(), getSkill().isSSPossible());
		realTarget.reduceCurrentHp(info.damage, getEffector(), getSkill(), true, true, false, true, false, false, getTemplate().isInstant(), getTemplate().isInstant(), info.crit, false, false, true);
		if(info.damage >= 1.0)
		{
			double lethalDmg = Formulas.calcLethalDamage(getEffector(), realTarget, getSkill());
			if(lethalDmg > 0.0)
				realTarget.reduceCurrentHp(lethalDmg, getEffector(), getSkill(), true, true, false, false, false, false, false);
		}
		return true;
	}
}
