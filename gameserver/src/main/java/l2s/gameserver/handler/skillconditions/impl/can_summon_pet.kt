package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.templates.StatsSet

/**
 * @author Sdw
 */
class can_summon_pet(params: StatsSet) : SkillCondition(params) {

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        val player = caster.player ?: return false

        /*if (PlayerConfig.RESTORE_PET_ON_RECONNECT && CharSummonTable.getInstance().getPets().containsKey(player.getObjectId())) {
            player.sendPacket(SystemMsg.YOU_MAY_NOT_USE_MULTIPLE_PETS_OR_SERVITORS_AT_THE_SAME_TIME)
            return false
        }*/
        if (player.hasServitor()) {
            player.sendPacket(SystemMsg.YOU_MAY_NOT_USE_MULTIPLE_PETS_OR_SERVITORS_AT_THE_SAME_TIME)
            return false
        }
        if (player.isProcessingRequest || player.isInStoreMode) {
            player.sendPacket(SystemMsg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_A_PRIVATE_STORE)
            return false
        }
        if (player.isInCombat) {
            player.sendPacket(SystemMsg.YOU_CANNOT_SUMMON_DURING_COMBAT)
            return false
        }
        /*if (player.isInAirShip()) {
            player.sendPacket(SystemMsg.A_SERVITOR_OR_PET_CANNOT_BE_SUMMONED_WHILE_ON_AN_AIRSHIP)
            return false
        }*/
        if (player.isInFlyingTransform || player.isMounted || player.isInObserverMode || player.isTeleporting) {
            return false
        }

        return true
    }

}