package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.templates.StatsSet

/**
 * @author UnAfraid
 */
class can_summon_cubic(params: StatsSet) : SkillCondition(params) {

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        val player = caster.player ?: return false

        if (caster.isAlikeDead) {
            return false
        }

        if (player.isInObserverMode || player.isMounted) {
            return false
        }

        return true
    }

}