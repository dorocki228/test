package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.templates.StatsSet

/**
 * @author UnAfraid
 */
class consume_body(params: StatsSet) : SkillCondition(params) {

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        if (target != null && target.isMonster) {
            if (target.isDead && target.isVisible) {
                return true
            }
        }

        if (caster.isPlayer) {
            caster.sendPacket(SystemMsg.INVALID_TARGET)
        }

        return false
    }

}