package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.FinishRotatingPacket;
import l2s.gameserver.network.l2.s2c.StartRotatingPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.templates.skill.EffectTemplate;

public class i_p_attack extends i_abstract_effect
{
	private final boolean _onCrit;
	private final boolean _directHp;
	private final boolean _turner;
	private final boolean _blow;
	private final boolean _static;

	public i_p_attack(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_onCrit = template.getParam().getBool("onCrit", false);
		_directHp = template.getParam().getBool("directHp", false);
		_turner = template.getParam().getBool("turner", false);
		_blow = template.getParam().getBool("blow", false);
		_static = template.getParam().getBool("static", false);
	}

	@Override
	public boolean checkCondition()
	{
		return !getEffected().isDead() && super.checkCondition();
	}

	@Override
	public void instantUse()
	{
		if(_turner && !getEffected().isInvul())
		{
			getEffected().broadcastPacket(new StartRotatingPacket(getEffected(), getEffected().getHeading(), 1, 65535));
			getEffected().broadcastPacket(new FinishRotatingPacket(getEffected(), getEffector().getHeading(), 65535));
			getEffected().setHeading(getEffector().getHeading());
			getEffected().sendPacket(new SystemMessagePacket(SystemMsg.S1S_EFFECT_CAN_BE_FELT).addSkillName(getSkill()));
		}
		Creature realTarget = isReflected() ? getEffector() : getEffected();
		double power = calc();
		if(getSkill().getId() == 10300 && realTarget.isMonster() && !realTarget.isRaid())
			power = realTarget.getCurrentHp() - 1.0;
		if(_static)
		{
			realTarget.reduceCurrentHp(power, getEffector(), getSkill(), true, true, _directHp, true, false, false, power != 0.0, true, false, false, false, false);
			return;
		}
		Formulas.AttackInfo info = Formulas.calcPhysDam(getEffector(), realTarget, getSkill(), 1.0, power, false, _blow, getSkill().isSSPossible(), _onCrit);
		if(info == null)
			return;
		realTarget.reduceCurrentHp(info.damage, getEffector(), getSkill(), true, true, _directHp, true, false, false, getTemplate().isInstant() && power != 0.0, getTemplate().isInstant(), info.crit || info.blow, info.miss, false, false);
		if(!info.miss || info.damage >= 1.0)
		{
			double lethalDmg = Formulas.calcLethalDamage(getEffector(), realTarget, getSkill());
			if(lethalDmg > 0.0)
				realTarget.reduceCurrentHp(lethalDmg, getEffector(), getSkill(), true, true, false, false, false, false, false);
			else if(!isReflected())
				realTarget.doCounterAttack(getSkill(), getEffector(), _blow);
		}
	}
}
