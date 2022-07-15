package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.templates.StatsSet

/**
 * @author Sdw
 */
class can_untransform(params: StatsSet) : SkillCondition(params) {

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        return false

        /* TODO
        var canUntransform = true
        val player = caster.player ?: return false
        if (player.isAlikeDead() || player.isCursedWeaponEquipped()) {
            canUntransform = false
        } else if (player.isInFlyingTransform && !player.isInsideZone(ZoneId.LANDING)) {
            player.sendPacket(SystemMsg.YOU_ARE_TOO_HIGH_TO_PERFORM_THIS_ACTION_PLEASE_LOWER_YOUR_ALTITUDE_AND_TRY_AGAIN) // TODO: check if message is retail like.
            canUntransform = false
        }

        return canUntransform*/
    }

}