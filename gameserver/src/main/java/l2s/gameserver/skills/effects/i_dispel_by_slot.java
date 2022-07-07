package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.AbnormalType;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.EffectsComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class i_dispel_by_slot extends i_abstract_effect
{
	private final AbnormalType _abnormalType;
	private final int _maxAbnormalLvl;
	private final boolean _self;

	public i_dispel_by_slot(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_abnormalType = template.getParam().getEnum("abnormal_type", AbnormalType.class);
		if(_abnormalType == AbnormalType.none)
			_maxAbnormalLvl = 0;
		else
			_maxAbnormalLvl = template.getParam().getInteger("max_abnormal_level", 0);
		_self = template.getEffectType() == EffectType.i_dispel_by_slot_myself;
	}

	@Override
	public boolean checkCondition()
	{
		return _maxAbnormalLvl != 0 && super.checkCondition();
	}

	@Override
	public void instantUse()
	{
		Creature target = _self ? getEffector() : getEffected();
        List<Abnormal> effects = new ArrayList<>(target.getAbnormalList().getEffects());
		effects.sort(EffectsComparator.getInstance());
		Collections.reverse(effects);

        List<Skill> dispelledSkills = new ArrayList<>();
        for(Abnormal effect : effects)
		{
			Skill effectSkill = effect.getSkill();
			if(effectSkill == null)
				continue;
			if(effectSkill.isToggle())
				continue;
			if(effectSkill.isPassive())
				continue;
			if(effect.getAbnormalType() != _abnormalType)
				continue;
			if(_maxAbnormalLvl != -1 && effect.getAbnormalLvl() > _maxAbnormalLvl)
				continue;

			if(!effect.isOffensive())
				effect.scheduleForReApply(30_000);

			effect.exit();

			if(effect.isHidden() || dispelledSkills.contains(effectSkill))
				continue;
			dispelledSkills.add(effectSkill);
			target.sendPacket(new SystemMessagePacket(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(effectSkill));
		}
	}
}
