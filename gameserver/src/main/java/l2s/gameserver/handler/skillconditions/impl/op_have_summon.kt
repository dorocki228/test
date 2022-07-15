package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.templates.StatsSet

/**
 * @author UnAfraid
 * @author Java-man
 */
class op_have_summon(params: StatsSet) : SkillCondition(params) {

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        if (!caster.hasServitor()) {
            if (caster.isPlayer) {
                caster.sendPacket(SystemMsg.YOU_CANNOT_USE_THE_SKILL_BECAUSE_THE_SERVITOR_HAS_NOT_BEEN_SUMMONED)
            }

            return false
        }

        return true
    }

}