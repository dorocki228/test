package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.templates.StatsSet

/**
 * @author Sdw
 * @author Java-man
 */
class can_transform(params: StatsSet) : SkillCondition(params) {

    // TODO: What to do with this?
    private val _transformId = params.getInteger("can_transform_param1")

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        if (target != null && target.isPlayer) {
            val player = target.player
            if (player == null || player.isAlikeDead || player.isCursedWeaponEquipped) {
                return false
            }
            if (player.isSitting) {
                player.sendPacket(SystemMsg.YOU_CANNOT_TRANSFORM_WHILE_SITTING)
                return false
            }
            if (player.isTransformed) {
                player.sendPacket(SystemMsg.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN)
                return false
            }
            if (player.isInWater) {
                player.sendPacket(SystemMsg.YOU_CANNOT_POLYMORPH_INTO_THE_DESIRED_FORM_IN_WATER)
                return false
            }
            if (player.isInFlyingTransform || player.isMounted) {
                player.sendPacket(SystemMsg.YOU_CANNOT_POLYMORPH_WHILE_RIDING_A_PET)
                return false
            }
            /*if (player.getStat().has(BooleanStat.TRANSFORM_DISABLE)) {
                player.sendPacket(SystemMsg.YOU_ARE_STILL_UNDER_TRANSFORMATION_PENALTY_AND_CANNOT_BE_POLYMORPHED)
                return false
            }*/
        }

        return true
    }

}