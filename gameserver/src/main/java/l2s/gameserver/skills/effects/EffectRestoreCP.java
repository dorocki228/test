package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectRestoreCP extends EffectRestore
{
	private int _addToCp;

	public EffectRestoreCP(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_addToCp = 0;
	}

	private int calcAddToCp()
	{
		if(getEffected().isHealBlocked())
			return 0;
		if(!getEffected().isPlayer())
			return 0;
		double power = calc();
		if(power <= 0.0)
			return 0;
		if(_percent)
			power *= getEffected().getMaxCp() / 100.0;
		if(!_staticPower && !_ignoreBonuses)
			power *= getEffected().calcStat(Stats.CPHEAL_EFFECTIVNESS, 100.0, getEffector(), getSkill()) / 100.0;
		double newCp = getEffected().getCurrentCp() + power;
		newCp = Math.max(0.0, Math.min(newCp, getEffected().getMaxCp() / 100.0 * getEffected().calcStat(Stats.CP_LIMIT, null, null)));
		return (int) Math.max(0.0, newCp - getEffected().getCurrentCp());
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_addToCp = calcAddToCp();
		if(!getTemplate().isInstant())
			return;
		if(_addToCp > 0)
		{
			if(getEffector() != getEffected())
				getEffected().sendPacket(new SystemMessagePacket(SystemMsg.S2_CP_HAS_BEEN_RESTORED_BY_C1).addName(getEffector()).addNumber(_addToCp));
			else
				getEffected().sendPacket(new SystemMessagePacket(SystemMsg.S1_CP_HAS_BEEN_RESTORED).addNumber(_addToCp));
			getEffected().setCurrentCp(getEffected().getCurrentCp() + _addToCp);
		}
	}

	@Override
	public boolean onActionTime()
	{
		if(getTemplate().isInstant())
			return false;
		if(_addToCp > 0)
			getEffected().setCurrentCp(getEffected().getCurrentCp() + _addToCp);
		return true;
	}
}
