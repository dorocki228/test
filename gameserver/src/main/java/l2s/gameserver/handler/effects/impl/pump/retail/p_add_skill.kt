package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.model.skill.SkillCaster
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.skills.SkillEntryType
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Add Skill effect implementation.
 * @author Java-man
 */
class p_add_skill(template: EffectTemplate) : EffectHandler(template) {

    private val skillEntry: SkillEntry

    init {
        val skill = params.getIntegerArray("p_add_skill_param1", ":")
        skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.TRIGGER, skill[0], skill[1])
    }

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.addSkill(skillEntry)
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.removeSkill(skillEntry)
    }

}
