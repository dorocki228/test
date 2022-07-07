package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.templates.StatsSet;

public class ChargeSoul extends Skill
{
	private final int _numSouls;

	public ChargeSoul(StatsSet set)
	{
		super(set);
		_numSouls = set.getInteger("numSouls", getLevel());
	}

	@Override
	protected void useSkill(Creature activeChar, Creature target, boolean reflected)
	{
		if(!activeChar.isPlayer())
			return;
		if(target.isDead())
			return;
		Creature realTarget = reflected ? activeChar : target;
		if(getPower() > 0.0)
		{
			Formulas.AttackInfo info = Formulas.calcPhysDam(activeChar, realTarget, this, false, false, isSSPossible(), false);
			if(info != null)
			{
				realTarget.reduceCurrentHp(info.damage, activeChar, this, true, true, false, true, false, false, true, true, info.crit || info.blow, false, false, false);
				if(!info.miss || info.damage >= 1.0)
				{
					double lethalDmg = Formulas.calcLethalDamage(activeChar, realTarget, this);
					if(lethalDmg > 0.0)
						realTarget.reduceCurrentHp(lethalDmg, activeChar, this, true, true, false, false, false, false, false);
					else if(!reflected)
						realTarget.doCounterAttack(this, activeChar, false);
				}
			}
		}
		if(realTarget.isPlayable() || realTarget.isMonster())
			activeChar.setConsumedSouls(activeChar.getConsumedSouls() + _numSouls, null);
	}
}
