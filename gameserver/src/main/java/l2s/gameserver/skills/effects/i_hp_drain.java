package l2s.gameserver.skills.effects;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;

public class i_hp_drain extends i_abstract_effect
{
	private final double _absorbPercent;

	public i_hp_drain(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_absorbPercent = template.getParam().getDouble("absorb_percent", 0.0);
	}

	@Override
	public boolean checkCondition()
	{
		return calc() > 0.0 && (getEffected().isPlayable() || !Config.DISABLE_VAMPIRIC_VS_MOB_ON_PVP) && getEffector().getPvpFlag() == 0 && super.checkCondition();
	}

	@Override
	public void instantUse()
	{
		Creature realTarget = isReflected() ? getEffector() : getEffected();
		if(realTarget.isDead())
			return;
		double targetHp = realTarget.getCurrentHp();
		double targetCP = realTarget.getCurrentCp();
		double damage = 0.0;
		if(getSkill().isMagic())
		{
			Formulas.AttackInfo info = Formulas.calcMagicDam(getEffector(), realTarget, getSkill(), calc(), getSkill().isSSPossible());
			realTarget.reduceCurrentHp(info.damage, getEffector(), getSkill(), true, true, false, true, false, false, true, true, info.crit, false, false, true);
			if(info.damage >= 1.0)
			{
				double lethalDmg = Formulas.calcLethalDamage(getEffector(), realTarget, getSkill());
				if(lethalDmg > 0.0)
					realTarget.reduceCurrentHp(lethalDmg, getEffector(), getSkill(), true, true, false, false, false, false, false);
			}
			damage = info.damage;
		}
		else
		{
			Formulas.AttackInfo info = Formulas.calcPhysDam(getEffector(), realTarget, getSkill(), 1.0, calc(), false, false, getSkill().isSSPossible(), false);
			if(info != null)
			{
				realTarget.reduceCurrentHp(info.damage, getEffector(), getSkill(), true, true, false, true, false, false, true, true, info.crit || info.blow, false, false, false);
				if(!info.miss || info.damage >= 1.0)
				{
					double lethalDmg = Formulas.calcLethalDamage(getEffector(), realTarget, getSkill());
					if(lethalDmg > 0.0)
						realTarget.reduceCurrentHp(lethalDmg, getEffector(), getSkill(), true, true, false, false, false, false, false);
					else if(!isReflected())
						realTarget.doCounterAttack(getSkill(), getEffector(), false);
				}
				damage = info.damage;
			}
		}
		if(_absorbPercent > 0.0 && !getEffector().isHealBlocked())
		{
			double hp = 0.0;
			if(damage > targetCP || !realTarget.isPlayer())
				hp = (damage - targetCP) * (_absorbPercent / 100.0);
			if(hp > targetHp)
				hp = targetHp;
			double addToHp = Math.max(0.0, Math.min(hp, getEffector().calcStat(Stats.HP_LIMIT, null, null) * getEffector().getMaxHp() / 100.0 - getEffector().getCurrentHp()));
			if(addToHp > 0.0)
				getEffector().setCurrentHp(getEffector().getCurrentHp() + addToHp, false);
		}
	}
}
