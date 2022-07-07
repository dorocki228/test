package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.EffectsComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class i_dispel_all extends i_abstract_effect
{
	public i_dispel_all(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public void instantUse()
	{
        List<Abnormal> effects = new ArrayList<>(getEffected().getAbnormalList().getEffects());
		effects.sort(EffectsComparator.getInstance());
		Collections.reverse(effects);

        List<Skill> dispelledSkills = new ArrayList<>();
        for(Abnormal effect : effects)
		{
			if(!effect.isCancelable())
				continue;
			Skill effectSkill = effect.getSkill();
			if(effectSkill == null)
				continue;
			if(effectSkill.isToggle())
				continue;
			if(effectSkill.isPassive())
				continue;
			if(getEffected().isSpecialEffect(effectSkill))
				continue;

			effect.scheduleForReApply(30_000);

			effect.exit();

			if(effect.isHidden() || dispelledSkills.contains(effectSkill))
				continue;
			dispelledSkills.add(effectSkill);
			getEffected().sendPacket(new SystemMessagePacket(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(effectSkill));
		}
	}
}
