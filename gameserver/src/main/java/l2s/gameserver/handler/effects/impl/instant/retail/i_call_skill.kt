package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.data.xml.holder.SkillHolder
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.model.skill.SkillCaster
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.skills.SkillEntryType
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

/**
 * Call Skill effect implementation.
 * @author NosBit
 * @author Java-man
 */
class i_call_skill(template: EffectTemplate) : i_abstract_effect(template) {

    private val skillEntries: Collection<SkillEntry>
    private val _maxIncreaseLevel: Int

    init {
        val skill = params.getIntegerArray("i_call_skill_param1", ":")
        val level = if (skill.size >= 2) skill[1] else 1
        val skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, skill[0], level)
        skillEntries = listOf(skillEntry)
        _maxIncreaseLevel = params.getInteger("max_increase_level", 0)

        require(skillEntries.isNotEmpty())
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        for (entry in skillEntries) {
            callSkill(caster, target, entry)
        }
    }

    private fun callSkill(caster: Creature, target: Creature, skillEntry: SkillEntry) {
        //Creature aimTarget = skill.getAimingTarget(effector, effected, skill, true, true, false);
        val triggerSkill = if (_maxIncreaseLevel <= 0) {
            skillEntry
        } else {
            var hasSkill: Skill? = null
            for (effect in target.abnormalList) {
                if (effect.skill.id == skill.id) {
                    hasSkill = effect.skill //taking the first one only.
                    break
                }
            }

            if (hasSkill == null) {
                loop@ for (servitor in target.servitors) {
                    for (effect in servitor.abnormalList) {
                        if (effect.skill.id == skill.id) {
                            hasSkill = effect.skill //taking the first one only.
                            break@loop
                        }
                    }
                }
            }

            if (hasSkill != null) {
                val newSkill = SkillHolder.getInstance().getSkill(skill.id, min(_maxIncreaseLevel, hasSkill.level + 1))
                SkillEntry.makeSkillEntry(SkillEntryType.NONE, newSkill ?: hasSkill)
            } else {
                skillEntry
            }
        }

        if (triggerSkill != null) {
            SkillCaster.triggerCast(caster, target, triggerSkill)
        }
    }

    override fun getCalledSkills(): Collection<SkillEntry> {
        return skillEntries
    }

}
