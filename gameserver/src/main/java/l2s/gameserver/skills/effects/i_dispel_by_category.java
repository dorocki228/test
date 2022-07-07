package l2s.gameserver.skills.effects;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.EffectsComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class i_dispel_by_category extends i_abstract_effect
{
	private final AbnormalCategory _abnormalCategory;
	private final int _dispelChance;
	private final int _maxCount;

	public i_dispel_by_category(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_abnormalCategory = template.getParam().getEnum("abnormal_category", AbnormalCategory.class);
		_dispelChance = template.getParam().getInteger("dispel_chance", 100);
		_maxCount = template.getParam().getInteger("max_count", 0);
	}

	@Override
	public boolean checkCondition()
	{
		return _dispelChance != 0 && _maxCount != 0 && super.checkCondition();
	}

	@Override
	public void instantUse()
	{
        List<Abnormal> effects = new ArrayList<>(getEffected().getAbnormalList().getEffects());
		effects.sort(EffectsComparator.getInstance());
		Collections.reverse(effects);

        List<Skill> notDispelledSkills = new ArrayList<>();
        List<Skill> dispelledSkills = new ArrayList<>();
        if(_abnormalCategory == AbnormalCategory.slot_debuff)
			for(Abnormal effect : effects)
			{
				if(!effect.isCancelable())
					continue;
				Skill effectSkill = effect.getSkill();
				if(effectSkill == null)
					continue;
				if(notDispelledSkills.contains(effectSkill))
					continue;
				boolean dispelled = dispelledSkills.contains(effectSkill);
				if(_maxCount > 0 && !dispelled && dispelledSkills.size() >= _maxCount)
					continue;
				if(!effect.isOffensive())
					continue;
				if(effectSkill.isToggle())
					continue;
				if(effectSkill.isPassive())
					continue;
				if(getEffected().isSpecialEffect(effectSkill))
					continue;
				if(effectSkill.getMagicLevel() <= 0)
					continue;

				if(dispelled || Rnd.chance(_dispelChance))
				{
					effect.exit();
					dispelledSkills.add(effectSkill);
					if(effect.isHidden() || dispelled)
						continue;
					getEffected().sendPacket(new SystemMessagePacket(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(effectSkill));
				}
				else
					notDispelledSkills.add(effectSkill);
			}
		else if(_abnormalCategory == AbnormalCategory.slot_buff)
			for(Abnormal effect : effects)
			{
				if(!effect.isCancelable())
					continue;
				Skill effectSkill = effect.getSkill();
				if(effectSkill == null)
					continue;
				if(notDispelledSkills.contains(effectSkill))
					continue;
				boolean dispelled = dispelledSkills.contains(effectSkill);
				if(_maxCount > 0 && !dispelled && dispelledSkills.size() >= _maxCount)
					continue;
				if(effect.isOffensive())
					continue;
				if(effectSkill.isToggle())
					continue;
				if(effectSkill.isPassive())
					continue;
				if(getEffected().isSpecialEffect(effectSkill))
					continue;
				if(effectSkill.getMagicLevel() <= 0)
					continue;

				if(dispelled || calcCancelChance(effect))
				{
					effect.scheduleForReApply(30_000);

					effect.exit();

					dispelledSkills.add(effectSkill);
					if(effect.isHidden() || dispelled)
						continue;
					getEffected().sendPacket(new SystemMessagePacket(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(effectSkill));
				}
				else
					notDispelledSkills.add(effectSkill);
			}
	}

	private boolean calcCancelChance(Abnormal effect)
	{
		double cancel_res_multiplier = getEffected().calcStat(Stats.CANCEL_RESIST, 0.0, null, null);
		int cancellMagicLvl = getSkill().getMagicLevel() > 0 ? getSkill().getMagicLevel() : getEffector().getLevel();
		int effectMagicLvl = effect.getSkill().getMagicLevel() > 0 ? effect.getSkill().getMagicLevel() : getEffected().getLevel();
		int dml = cancellMagicLvl - effectMagicLvl;
		if(dml <= 8)
			return false;
		int buff_duration = effect.getTimeLeft();
		cancel_res_multiplier = 1.0 - cancel_res_multiplier * 0.01;
		double prelim_chance = (2.0 * dml + _dispelChance + buff_duration / 120) * cancel_res_multiplier;
		prelim_chance = Math.max(Math.min(prelim_chance, 75.0), 25.0);
		return Rnd.chance(prelim_chance);
	}

	private enum AbnormalCategory
	{
		slot_buff,
		slot_debuff
    }
}
