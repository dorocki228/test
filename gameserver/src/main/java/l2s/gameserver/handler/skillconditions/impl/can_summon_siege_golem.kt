package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.data.xml.holder.ResidenceHolder
import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.entity.events.impl.SiegeEvent
import l2s.gameserver.model.entity.residence.Castle
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.templates.StatsSet

/**
 * @author Sdw
 */
class can_summon_siege_golem(params: StatsSet) : SkillCondition(params) {

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        if (!caster.isPlayer) {
            return false
        }

        val player = caster.player ?: return false
        if (player.isAlikeDead || player.isCursedWeaponEquipped || player.clan == null) {
            return false
        }

        val castle: Castle? = ResidenceHolder.getInstance().getResidenceByObject(Castle::class.java, player)
        if (castle == null) {
            return false
        }

        if (castle.id == 0) {
            player.sendPacket(SystemMsg.INVALID_TARGET)
            return false
        } else if (!castle.siegeEvent.isInProgress) {
            player.sendPacket(SystemMsg.INVALID_TARGET)
            return false
        } else if (player.clanId != 0 && castle.siegeEvent.getSiegeClan(SiegeEvent.ATTACKERS, player.clanId) == null) {
            player.sendPacket(SystemMsg.INVALID_TARGET)
            return false
        }

        return true
    }

}