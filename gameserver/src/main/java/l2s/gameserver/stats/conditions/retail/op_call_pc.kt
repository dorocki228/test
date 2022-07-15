package l2s.gameserver.stats.conditions.retail

import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.Zone
import l2s.gameserver.model.items.ItemInstance
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.stats.conditions.Condition

class op_call_pc : Condition() {

    override fun testImpl(
        actor: Creature,
        target: Creature?,
        skill: Skill?,
        item: ItemInstance?,
        value: Double
    ): Boolean {
        var canCallPlayer = true
        val player = actor.player
        if (player == null) {
            canCallPlayer = false
        } else if (player.isInOlympiadMode) {
            player.sendPacket(SystemMsg.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD)
            canCallPlayer = false
        } else if (player.isInObserverMode) {
            canCallPlayer = false
        } else if (player.isInZone(Zone.ZoneType.no_summon) || player.isFlying) {
            player.sendPacket(SystemMsg.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING)
            canCallPlayer = false
        }

        return canCallPlayer
    }

}
