package l2s.gameserver.skills.effects;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectCallSkills extends Abnormal
{
	public EffectCallSkills(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		int[] skillIds = getTemplate().getParam().getIntegerArray("skillIds");
		int[] skillLevels = getTemplate().getParam().getIntegerArray("skillLevels");
		for(int i = 0; i < skillIds.length; ++i)
		{
			Skill skill = SkillHolder.getInstance().getSkill(skillIds[i], skillLevels[i]);
			if(!skill.isNotBroadcastable())
				for(Creature cha : skill.getTargets(getEffector(), getEffected(), false))
					getEffector().broadcastPacket(new MagicSkillUse(getEffector(), cha, skillIds[i], skillLevels[i], 0, 0L));
			getEffector().callSkill(skill, skill.getTargets(getEffector(), getEffected(), false), false, false);
		}
	}
}
