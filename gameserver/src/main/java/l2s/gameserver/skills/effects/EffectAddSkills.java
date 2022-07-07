package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectAddSkills extends Abnormal
{
	public EffectAddSkills(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		for(Skill.AddedSkill as : getSkill().getAddedSkills())
		{
			SkillEntry skillEntry = as.getSkill();
			if(skillEntry != null)
				getEffected().addSkill(skillEntry);
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();
		for(Skill.AddedSkill as : getSkill().getAddedSkills())
		{
			SkillEntry skillEntry = as.getSkill();
			if(skillEntry != null)
				getEffected().removeSkill(skillEntry);
		}
	}
}
