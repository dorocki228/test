package l2s.gameserver.skills.effects;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo.MagicAttackType;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectGetEffects extends Abnormal
{
	private final Skill _effectsSkill;

	public EffectGetEffects(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		int skillId = template.getParam().getInteger("effects_skill_id");
		int skillLvl = template.getParam().getInteger("effects_skill_level", 1);
		_effectsSkill = SkillHolder.getInstance().getSkill(skillId, skillLvl);
	}

	@Override
	public boolean checkCondition()
	{
		if(_effectsSkill == null)
			return false;
		int chance = _effectsSkill.getActivateRate();
		if(chance >= 0 && !Formulas.calcSkillSuccess(getEffector(), getEffected(), _effectsSkill, chance))
		{
			if(getEffected() == getEffector().getCastingTarget() && getSkill() == getEffector().getCastingSkill())
			{
				ExMagicAttackInfo.packet(getEffector(), getEffected(), MagicAttackType.RESISTED);
				getEffector().sendPacket(new SystemMessagePacket(SystemMsg.S1_HAS_FAILED).addSkillName(_effectsSkill));
			}
			return false;
		}
		return super.checkCondition();
	}

	@Override
	public void instantUse()
	{
		_effectsSkill.getEffects(getEffector(), getEffected());
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(!getTemplate().isInstant() && !_effectsSkill.getEffects(getEffector(), getEffected(), getTimeLeft() * 1000, 1.0))
			exit();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(!getTemplate().isInstant())
			getEffected().getAbnormalList().stopEffects(_effectsSkill);
	}
}
