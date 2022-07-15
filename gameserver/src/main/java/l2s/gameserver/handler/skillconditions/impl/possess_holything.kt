package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.data.xml.holder.ResidenceHolder
import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent
import l2s.gameserver.model.entity.residence.Castle
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.SystemMessagePacket
import l2s.gameserver.templates.StatsSet
import l2s.gameserver.utils.PositionUtils


/**
 * @author UnAfraid
 * @author Java-man
 */
class possess_holything(params: StatsSet) : SkillCondition(params) {

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        if (target == null) {
            return false
        }

        val player = caster.player ?: return false

        if (player.isAlikeDead || player.isCursedWeaponEquipped || !player.isClanLeader) {
            return false
        }

        val castle: Castle? = ResidenceHolder.getInstance().getResidenceByObject(Castle::class.java, player)
        if (castle == null || castle.id <= 0) {
            val sm = SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS)
            sm.addSkillName(skill)
            player.sendPacket(sm)
            return false
        }

        if (!PositionUtils.checkIfInRange(skill.castRange, player, target, true)) {
            player.sendPacket(SystemMsg.THE_DISTANCE_IS_TOO_FAR_AND_SO_THE_CASTING_HAS_BEEN_STOPPED)
            return false
        }

        val siegeEvents = player.getEvents(CastleSiegeEvent::class.java)
        if (siegeEvents.isEmpty()) {
            player.sendPacket(SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS)
                    .addSkillName(skill))
            return false
        }

        val targetEvents = target.getEvents(CastleSiegeEvent::class.java)
        if (targetEvents.isEmpty()) {
            player.sendPacket(SystemMsg.INVALID_TARGET)
            return false
        }

        val clan = player.clan

        var success = false
        for (event in targetEvents) {
            if (!siegeEvents.contains(event)) {
                continue
            }
            if (event.getSiegeClan(CastleSiegeEvent.ATTACKERS, clan) == null) {
                continue
            }
            if (!event.canCastSeal(player)) {
                // TODO: Должно ли быть особое сообщение?
                continue
            }

            event.broadcastTo(SystemMsg.THE_OPPOSING_CLAN_HAS_STARTED_TO_ENGRAVE_THE_HOLY_ARTIFACT, CastleSiegeEvent.DEFENDERS)

            success = true
        }

        if (!success) {
            player.sendPacket(SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skill));
            return false;
        }

        return true;
    }

}