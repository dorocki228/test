package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Skill.SkillType;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectInvulnerable extends Abnormal
{
	public EffectInvulnerable(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean checkCondition()
	{
		Skill skill = _effected.getCastingSkill();
		return (skill == null || skill.getSkillType() != Skill.SkillType.TAKECASTLE && skill.getSkillType() != SkillType.TAKEFORTRESS) && super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.addInvulnerableEffect(this);
	}

	@Override
	public void onExit()
	{
		_effected.removeInvulnerableEffect(this);
		super.onExit();
	}
}
