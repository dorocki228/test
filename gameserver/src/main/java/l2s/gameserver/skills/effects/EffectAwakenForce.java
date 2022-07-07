package l2s.gameserver.skills.effects;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectAwakenForce extends Abnormal
{
	private static final int SIGEL_SKILL_ID = 1928;
	private static final int TYR_SKILL_ID = 1930;
	private static final int ODAL_SKILL_ID = 1932;
	private static final int AEORE_SKILL_ID = 1934;
	private static final int FEO_SKILL_ID = 1936;
	private static final int WYNN_SKILL_ID = 1938;
	private static final int ALGIZA_SKILL_ID = 1940;
	private static final int SOLIDARITY_SKILL_ID = 1955;

	public EffectAwakenForce(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	public void checkSolidarity()
	{
		int skillId = getSkill().getId();
        int i = 0;
		for(Abnormal effect : _effected.getAbnormalList().getEffects())
		{
			if(effect.getEffectType() != EffectType.AwakenForce)
				continue;
			int effectSkillId = effect.getSkill().getId();
			if(effectSkillId != 1928 && effectSkillId != 1930 && effectSkillId != 1932 && effectSkillId != 1934 && effectSkillId != 1936 && effectSkillId != 1938 && effectSkillId != 1940)
				continue;
			++i;
		}
		_effected.getAbnormalList().stopEffects(1955);
        int solidarityLevel = 0;
        if(i >= 0 && i <= 2)
		{
			solidarityLevel = 0;
			return;
		}
		if(i == 3 || i == 4)
			solidarityLevel = 1;
		else if(i == 5)
			solidarityLevel = 2;
		else if(i >= 6)
			solidarityLevel = 3;
		SkillHolder.getInstance().getSkill(1955, solidarityLevel).getEffects(getEffector(), getEffected());
	}

	@Override
	public void onStart()
	{
		super.onStart();
		checkSolidarity();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		checkSolidarity();
	}
}
