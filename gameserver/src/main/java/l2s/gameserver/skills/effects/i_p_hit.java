package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.templates.skill.EffectTemplate;

public class i_p_hit extends i_abstract_effect
{
	public i_p_hit(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean checkCondition()
	{
		return !getEffected().isDead() && super.checkCondition();
	}

	@Override
	public void instantUse()
	{
		boolean dual = false;
		switch(getEffector().getBaseStats().getAttackType())
		{
			case DUAL:
			case DUALFIST:
			{
				dual = true;
				break;
			}
		}
		int damage = 0;
		boolean shld = false;
		boolean crit = false;
		boolean miss = false;
		Formulas.AttackInfo info = Formulas.calcPhysDam(getEffector(), getEffected(), null, calc(), 0.0, dual, false, getEffector().getChargedSoulshotPower() > 0.0, false);
		if(info != null)
		{
			damage = (int) info.damage;
			shld = info.shld;
			crit = info.crit;
			miss = info.miss;
		}
		getEffected().reduceCurrentHp(damage, getEffector(), null, true, true, false, true, false, false, true, true, crit, miss, shld, false);
		if(dual)
		{
			damage = 0;
			shld = false;
			crit = false;
			miss = false;
			info = Formulas.calcPhysDam(getEffector(), getEffected(), null, calc(), 0.0, dual, false, getEffector().getChargedSoulshotPower() > 0.0, false);
			if(info != null)
			{
				damage = (int) info.damage;
				shld = info.shld;
				crit = info.crit;
				miss = info.miss;
			}
			getEffected().reduceCurrentHp(damage, getEffector(), null, true, true, false, true, false, false, true, true, crit, miss, shld, false);
		}
	}
}
