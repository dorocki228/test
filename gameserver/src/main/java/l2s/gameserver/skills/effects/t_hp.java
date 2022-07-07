package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo.MagicAttackType;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;

public class t_hp extends Abnormal
{
	private final boolean _percent;

	public t_hp(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_percent = getTemplate().getParam().getBool("percent", false);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		giveDamage(true);
	}

	@Override
	public boolean onActionTime()
	{
		return giveDamage(false);
	}

	private boolean giveDamage(boolean first)
	{
		if(getEffected().isDead())
			return getSkill().isToggle();

		double hp = calc();
		if(_percent)
			hp *= getEffected().getMaxHp() / 100;

		double power = getEffector().calcStat(getSkill().isMagic() ? Stats.INFLICTS_M_DAMAGE_POWER : Stats.INFLICTS_P_DAMAGE_POWER, Math.abs(hp), getEffected(), getSkill());

		boolean crit = false;
		boolean isMagic = getSkill().isMagic();
		if(first)
		{
			crit = Formulas.calcMCrit(getEffector(), getEffected(), getSkill());
			if(!isMagic || !crit)
				return false;

			if(hp > 0)
			{
				ExMagicAttackInfo.packet(getEffector(), getEffected(), MagicAttackType.CRITICAL_HEAL);
				getEffector().sendPacket(SystemMsg.MAGIC_CRITICAL_HIT);
			}

			power *= getDuration();

			if(hp > 0)
				power /= getInterval();

		}
		else if(hp < 0.0)
			power *= getInterval();

		if(hp < 0.0)
		{

			if(power > getEffected().getCurrentHp() - 1.0 && !getEffected().isNpc())
			{
				if(!getSkill().isOffensive() && !isArcanePower())
					getEffected().sendPacket(SystemMsg.NOT_ENOUGH_HP);
				return isArcanePower();
			}

			if(getSkill().getAbsorbPart() > 0.0)
				getEffector().setCurrentHp(getSkill().getAbsorbPart() * Math.min(getEffected().getCurrentHp(), power) + getEffector().getCurrentHp(), false);

			boolean awake = !getEffected().isNpc() && getEffected() != getEffector();
			boolean standUp = getEffected() != getEffector();
			boolean directHp = getEffector().isNpc() || getEffected() == getEffector();

			getEffected().reduceCurrentHp(power, getEffector(), getSkill(), awake, standUp, directHp, false, false, true, true, true, crit, false, false, isMagic);
		}
		else if(hp > 0)
		{
			if(getEffector() != getEffected())
				getEffected().sendPacket(new SystemMessagePacket(SystemMsg.S2_HP_HAS_BEEN_RESTORED_BY_C1).addName(getEffector()).addNumber((int) power).addHpChange(getEffected().getObjectId(), getEffector().getObjectId(), (int) power));
			else
				getEffected().sendPacket(new SystemMessagePacket(SystemMsg.S1_HP_HAS_BEEN_RESTORED).addNumber((int) power).addHpChange(getEffected().getObjectId(), getEffector().getObjectId(), (int) power));

			getEffected().setCurrentHp(getEffected().getCurrentHp() + power, false);
		}

		return true;
	}

	private boolean isArcanePower() {
		return getSkill().getId() == 337;
	}
}
