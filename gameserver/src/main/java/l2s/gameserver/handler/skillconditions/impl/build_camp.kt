package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.data.xml.holder.ResidenceHolder
import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.Zone
import l2s.gameserver.model.entity.events.impl.SiegeEvent
import l2s.gameserver.model.entity.residence.Castle
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.SystemMessagePacket
import l2s.gameserver.templates.StatsSet

/**
 * @author Sdw
 * @author Java-man
 */
class build_camp(params: StatsSet) : SkillCondition(params) {

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        val player = caster.player ?: return false

        val clan = player.clan
        if (player.isAlikeDead || player.isCursedWeaponEquipped || clan == null) {
            return false
        }

        val castle: Castle? = ResidenceHolder.getInstance().getResidenceByObject(Castle::class.java, player)
        val sm: SystemMessagePacket
        if (castle == null) {
            sm = SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS)
            sm.addSkillName(skill)
            player.sendPacket(sm)
            return false
        }
        if (!castle.siegeEvent.isInProgress) {
            sm = SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS)
            sm.addSkillName(skill)
            player.sendPacket(sm)
            return false
        }
        if (castle.siegeEvent.getSiegeClan(SiegeEvent.ATTACKERS, clan) == null) {
            sm = SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS)
            sm.addSkillName(skill)
            player.sendPacket(sm)
            return false
        }
        if (!player.isClanLeader) {
            sm = SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS)
            sm.addSkillName(skill)
            player.sendPacket(sm)
            return false
        }
        if (castle.siegeEvent.getSiegeClan(SiegeEvent.ATTACKERS, clan).flag != null) {
            sm = SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS)
            sm.addSkillName(skill)
            player.sendPacket(sm)
            return false
        }
        if (!player.isInZone(Zone.ZoneType.HEADQUARTER)) {
            player.sendPacket(SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE)
            return false
        }

        return true
    }

}