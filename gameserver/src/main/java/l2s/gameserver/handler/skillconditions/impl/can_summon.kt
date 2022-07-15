package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.templates.StatsSet

/**
 * @author Sdw
 */
class can_summon(params: StatsSet) : SkillCondition(params) {

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        val player = caster.player ?: return false

        if (player.isInFlyingTransform || player.isMounted || player.isInObserverMode || player.isTeleporting) {
            return false
        }
        /*if (player.isInAirShip()) {
            player.sendPacket(SystemMsg.A_SERVITOR_OR_PET_CANNOT_BE_SUMMONED_WHILE_ON_AN_AIRSHIP)
           return false
        }*/

        return true
    }

}