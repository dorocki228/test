package l2s.gameserver.skills.effects;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Skill.SkillType;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectRemoveTarget extends Abnormal
{
	private final boolean _stopTarget;

	public EffectRemoveTarget(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_stopTarget = template.getParam().getBool("stop_target", false);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if (getEffected().isPlayer()&& getEffected().isInvul())
			return;
		
		if(getEffected().getAI() instanceof DefaultAI)
			((DefaultAI) getEffected().getAI()).setGlobalAggro(System.currentTimeMillis() + 3000L);
		getEffected().setTarget(null);
		if(_stopTarget)
			getEffected().stopMove();
		getEffected().abortAttack(true, true);
		Skill castingSkill = getEffected().getCastingSkill();
		if(castingSkill == null || castingSkill.getSkillType() != Skill.SkillType.TAKECASTLE && castingSkill.getSkillType() != SkillType.TAKEFORTRESS)
			getEffected().abortCast(true, true);
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, getEffector());
	}
}
