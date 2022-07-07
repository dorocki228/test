package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectNegateMark extends Abnormal
{
	private static final int MARK_OF_WEAKNESS = 11259;
	private static final int MARK_OF_PLAGUE = 11261;
	private static final int MARK_OF_TRICK = 11262;

	public EffectNegateMark(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public void onStart()
	{
		Creature effected = getEffected();
		Creature effector = getEffector();
		Skill skill = getSkill();
		byte markCount = 0;
		for(Abnormal effect : effected.getAbnormalList().getEffects())
		{
			int skillId = effect.getSkill().getId();
			if(skillId == 11259 || skillId == 11261 || skillId == 11262)
			{
				++markCount;
				effected.getAbnormalList().stopEffects(skillId);
			}
		}
		if(markCount > 0)
		{
			Formulas.AttackInfo info = Formulas.calcMagicDam(effector, effected, skill, getSkill().isSSPossible());
			effected.reduceCurrentHp(info.damage * markCount, effector, skill, true, true, false, true, false, false, true, true, info.crit, false, false, true);
		}
	}
}
