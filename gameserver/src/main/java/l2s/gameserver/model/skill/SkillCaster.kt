package l2s.gameserver.model.skill

import l2s.gameserver.model.Creature
import l2s.gameserver.network.l2.s2c.MagicSkillUse
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.skills.SkillEntryType

class SkillCaster {

    companion object {
        fun triggerCast(
                caster: Creature,
                target: Creature?,
                skillEntry: SkillEntry,
                ignoreTargetType: Boolean = true
        ) {
            val skill = skillEntry.template

            if (skill.reuseDelay > 0 && caster.isSkillDisabled(skill)) {
                return
            }

            val newTarget = when {
                ignoreTargetType -> target
                else -> skill.targetTypeNew.getTarget(caster, target, skill, false, false, false)
            }

            if (!skillEntry.checkCondition(caster, target, true, true, true, false, true)) {
                return
            }

            val targets = skill.getTargets(caster, newTarget, skillEntry, false, true, false)

            if (!skill.isNotBroadcastable && !caster.isCastingNow) {
                for (cha in targets) {
                    if (cha != null) {
                        val packet = MagicSkillUse(caster, cha, skill.displayId, skill.displayLevel, 0, 0)
                        caster.broadcastPacket(packet)
                    }
                }
            }

            caster.callSkill(newTarget, SkillEntry.makeSkillEntry(SkillEntryType.NONE, skill), targets, false, true)
            caster.disableSkill(skill, skill.reuseDelay.toLong())
        }
    }

}