package l2s.gameserver.skills.effects;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectRestoreMP extends EffectRestore
{
	private int _addToMp;

	public EffectRestoreMP(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_addToMp = 0;
	}

	private int calcAddToMp()
	{
		if(getEffected().isHealBlocked())
			return 0;
		double power = calc();
		if(power <= 0.0)
			return 0;
		if(!_staticPower && !_percent && getSkill().isSSPossible() && Config.MANAHEAL_SPS_BONUS)
			power *= 1.0 + (200.0 + getEffector().getChargedSpiritshotPower()) * 0.001;
		if(_percent)
			power *= getEffected().getMaxMp() / 100.0;
		if(!_staticPower)
			if(!_ignoreBonuses)
			{
				if(_percent || getEffector() != getEffected())
					power *= getEffected().calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100.0, getEffector(), getSkill()) / 100.0;
			}
			else if(!_percent)
				power *= 1.7;

		if(!_staticPower && !_percent && getSkill().getMagicLevel() > 0 && getEffector() != getEffected())
		{
			int diff = getEffected().getLevel() - getSkill().getMagicLevel();
			if(diff > 5)
				if(diff < 20)
					power = (power / 100.0) * (100 - diff * 5);
				else
					power = 0.0;

		}

		double newMp = getEffected().getCurrentMp() + power;
		newMp = Math.max(0.0, Math.min(newMp, getEffected().getMaxMp() / 100.0 * getEffected().calcStat(Stats.MP_LIMIT, null, null)));
		return (int) Math.max(0.0, newMp - getEffected().getCurrentMp());
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_addToMp = calcAddToMp();
		if(!getTemplate().isInstant())
			return;
		if(_addToMp > 0)
		{
			if(getEffector() != getEffected())
				getEffected().sendPacket(new SystemMessagePacket(SystemMsg.S2_MP_HAS_BEEN_RESTORED_BY_C1).addName(getEffector()).addNumber(_addToMp));
			else
				getEffected().sendPacket(new SystemMessagePacket(SystemMsg.S1_MP_HAS_BEEN_RESTORED).addNumber(_addToMp));
			getEffected().setCurrentMp(getEffected().getCurrentMp() + _addToMp);
		}
	}

	@Override
	public boolean onActionTime()
	{
		if(getTemplate().isInstant())
			return false;
		if(_addToMp > 0)
			getEffected().setCurrentMp(getEffected().getCurrentMp() + _addToMp);
		return true;
	}
}
