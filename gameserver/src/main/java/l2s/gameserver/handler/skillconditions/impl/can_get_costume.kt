package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.data.xml.holder.CostumesHolder
import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.templates.StatsSet

/**
 * @author Bonux
 * @author Java-man
 */
class can_get_costume(params: StatsSet) : SkillCondition(params) {

    private val costumeIds: Sequence<Int>

    init {
        val temp = params.getString("id", "")
                .split(";")
                .filter { it.isNotBlank() }
                .map { it.toInt() }
                .asSequence()

        val grades = params.getString("grade", "")
                .split(";")
                .filter { it.isNotBlank() }
                .map { it.toInt() }
                .asSequence()

        costumeIds = temp + grades
    }

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

            val locationId = player.locationId
            return costumeIds
                    .map { CostumesHolder.getInstance().getCostume(it) }
                    .onEach { requireNotNull(it) }
                    .any { it.locationId == -1 || it.locationId == locationId }
        }

        return false
    }

}