package l2s.gameserver.skills.effects;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.AbnormalType;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.EffectsComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class i_dispel_by_slot_probability extends i_abstract_effect
{
	private final AbnormalType _abnormalType;
	private final int _dispelChance;

	public i_dispel_by_slot_probability(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_abnormalType = template.getParam().getEnum("abnormal_type", AbnormalType.class);
		if(_abnormalType == AbnormalType.none)
			_dispelChance = 0;
		else
			_dispelChance = template.getParam().getInteger("dispel_chance", 100);
	}

	@Override
	public boolean checkCondition()
	{
		return _dispelChance != 0 && super.checkCondition();
	}

	@Override
	public void instantUse()
	{
		Creature effected = getEffected();

		List<Abnormal> effects = new ArrayList<>(effected.getAbnormalList().getEffects());
		effects.sort(EffectsComparator.getInstance());
		Collections.reverse(effects);

        List<Skill> notDispelledSkills = new ArrayList<>();
        List<Skill> dispelledSkills = new ArrayList<>();
        for(Abnormal effect : effects)
		{
			if(!effect.isCancelable())
				continue;
			Skill effectSkill = effect.getSkill();
			if(effectSkill == null)
				continue;
			if(notDispelledSkills.contains(effectSkill))
				continue;
			if(effectSkill.isToggle())
				continue;
			if(effectSkill.isPassive())
				continue;
			if(effect.getAbnormalType() != _abnormalType)
				continue;
			boolean dispelled = dispelledSkills.contains(effectSkill);
			if(dispelled || Rnd.chance(_dispelChance))
			{
				effect.scheduleForReApply(30_000);

				effect.exit();

				dispelledSkills.add(effectSkill);
				if(effect.isHidden() || dispelled)
					continue;
				effected.sendPacket(new SystemMessagePacket(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(effectSkill));
			}
			else
				notDispelledSkills.add(effectSkill);
		}
	}
}
