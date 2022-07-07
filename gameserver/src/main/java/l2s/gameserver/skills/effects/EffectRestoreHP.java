package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo.MagicAttackType;
import l2s.gameserver.network.l2.s2c.ExRegenMaxPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectRestoreHP extends EffectRestore
{
	private final boolean _cpIncluding;
	private double _power;

	public EffectRestoreHP(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_power = 0.0;
		_cpIncluding = template.getParam().getBool("cp_including", false);
	}

	private int[] calcAddToHpCp()
	{
		double power = _power;
		int addToHp = (int) power;
		int addToCp = 0;
		if(_cpIncluding && getEffected().isPlayer())
		{
			double newHp = getEffected().getCurrentHp() + power;
			newHp = Math.max(0.0, Math.min(newHp, getEffected().getMaxHp() / 100.0 * getEffected().calcStat(Stats.HP_LIMIT, null, null)));
			addToHp = (int) Math.max(0.0, newHp - getEffected().getCurrentHp());
			if(_percent)
			{
				if(addToHp > 0)
					power = 0.0;
				else
				{
					power = getEffected().getMaxCp() / 100.0 * calc();
					if(!_ignoreBonuses)
						power *= getEffected().calcStat(Stats.CPHEAL_EFFECTIVNESS, 100.0, getEffector(), getSkill()) / 100.0;
				}
			}
			else
				power -= addToHp;
			if(power > 0.0)
			{
				double newCp = getEffected().getCurrentCp() + power;
				newCp = Math.max(0.0, Math.min(newCp, getEffected().getMaxCp() / 100.0 * getEffected().calcStat(Stats.CP_LIMIT, null, null)));
				addToCp = (int) Math.max(0.0, newCp - getEffected().getCurrentCp());
			}
		}
		return new int[] { addToHp, addToCp };
	}

    @Override
    public boolean checkCondition()
    {
        if(getEffected().isHealBlocked())
            return false;

        return super.checkCondition();
    }

	@Override
	public void onStart()
	{
		super.onStart();
		_power = calc();

		if(_power <= 0.0)
			return;

		if(getEffected().isHealBlocked())
			return;

		if(!_staticPower && !_percent)
		{
			_power += 0.1 * _power * Math.sqrt(getEffector().getMAtk(null, getSkill()) / 333);
			if(getSkill().isSSPossible() && getSkill().getHpConsume() == 0)
				_power *= 1.0 + (200.0 + getEffector().getChargedSpiritshotPower()) * 0.001;
			if(getSkill().isMagic() && Formulas.calcMCrit(getEffector(), getEffected(), getSkill()))
			{
				_power *= 2.0;

				ExMagicAttackInfo.packet(getEffector(), getEffected(), MagicAttackType.CRITICAL_HEAL);

				getEffector().sendPacket(SystemMsg.MAGIC_CRITICAL_HIT);
			}
		}
		if(_percent)
			_power *= getEffected().getMaxHp() / 100.0;
		if(!_staticPower && !_ignoreBonuses)
		{
			_power *= getEffected().calcStat(Stats.HEAL_EFFECTIVNESS, 100.0, getEffector(), getSkill()) / 100.0;
			_power = getEffector().calcStat(Stats.HEAL_POWER, _power, getEffected(), getSkill());
		}

		int[] addToHpCp = calcAddToHpCp();
		int addToHp = addToHpCp[0];
		if(!getTemplate().isInstant())
		{
			if(getEffected().isPlayer())
				getEffected().sendPacket(new ExRegenMaxPacket(addToHp, getDuration(), getInterval()));
			return;
		}
		if(addToHp > 0)
		{
			if(getSkill().getId() == 4051)
				getEffected().sendPacket(SystemMsg.REJUVENATING_HP);
			else if(getEffector() != getEffected())
				getEffected().sendPacket(new SystemMessagePacket(SystemMsg.S2_HP_HAS_BEEN_RESTORED_BY_C1).addName(getEffector()).addNumber(addToHp).addHpChange(getEffected().getObjectId(), getEffector().getObjectId(), addToHp));
			else
				getEffected().sendPacket(new SystemMessagePacket(SystemMsg.S1_HP_HAS_BEEN_RESTORED).addNumber(addToHp).addHpChange(getEffected().getObjectId(), getEffector().getObjectId(), addToHp));
			getEffected().setCurrentHp(getEffected().getCurrentHp() + addToHp, false);
		}
		int addToCp = addToHpCp[1];
		if(addToCp > 0)
		{
			if(getEffector() != getEffected())
				getEffected().sendPacket(new SystemMessagePacket(SystemMsg.S2_CP_HAS_BEEN_RESTORED_BY_C1).addName(getEffector()).addNumber(addToCp));
			else
				getEffected().sendPacket(new SystemMessagePacket(SystemMsg.S1_CP_HAS_BEEN_RESTORED).addNumber(addToCp));
			getEffected().setCurrentCp(getEffected().getCurrentCp() + addToCp);
		}
	}

	@Override
	public boolean onActionTime()
	{
		if(getTemplate().isInstant())
			return false;
		if(getEffected().isHealBlocked())
			return true;
		int[] addToHpCp = calcAddToHpCp();
		int addToHp = addToHpCp[0];
		if(addToHp > 0)
			getEffected().setCurrentHp(getEffected().getCurrentHp() + addToHp, false);
		int addToCp = addToHpCp[1];
		if(addToCp > 0)
			getEffected().setCurrentCp(getEffected().getCurrentCp() + addToCp);
		return true;
	}
}
