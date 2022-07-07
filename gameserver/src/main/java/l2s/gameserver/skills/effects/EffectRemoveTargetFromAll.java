package l2s.gameserver.skills.effects;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectRemoveTargetFromAll extends Abnormal
{
	public EffectRemoveTargetFromAll(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		for(Creature creature : getEffector().getAroundCharacters(1000, 0))
			if(creature.getTarget() == getEffector())
			{
				if (creature.isPlayer()&& creature.isInvul())
					return;
				if(creature.getAI() instanceof DefaultAI)
					((DefaultAI) creature.getAI()).setGlobalAggro(System.currentTimeMillis() + 3000L);
				creature.setTarget(null);
				creature.stopMove();
				creature.abortAttack(true, true);
				Skill castingSkill = creature.getCastingSkill();
				if(castingSkill == null || castingSkill.getSkillType() != Skill.SkillType.TAKECASTLE && castingSkill.getSkillType() != Skill.SkillType.TAKEFORTRESS)
					creature.abortCast(true, true);
				creature.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, getEffector());
			}
	}
}
