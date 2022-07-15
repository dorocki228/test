package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.Zone.ZoneType
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.templates.StatsSet

/**
 * @author Sdw
 */
class op_call_pc(params: StatsSet) : SkillCondition(params) {

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        val player = caster.player ?: return false

        if (player.isInOlympiadMode) {
            player.sendPacket(SystemMsg.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD)
            return false
        }
        if (player.isInObserverMode) {
            return false
        }
        if (player.isInZone(ZoneType.no_summon) || player.isInJail || player.isInFlyingTransform) {
            player.sendPacket(SystemMsg.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING)
            return false
        }

        return true
    }

}