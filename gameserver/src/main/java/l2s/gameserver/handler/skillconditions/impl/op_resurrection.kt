package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.listener.actor.player.impl.ReviveAnswerListener
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.SystemMessagePacket
import l2s.gameserver.templates.StatsSet

class op_resurrection(params: StatsSet) : SkillCondition(params) {

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        if (target == null) {
            return false
        }

        var canResurrect = true

        if (target == caster) {
            return canResurrect
        }

        val player = caster.player
        val targetPlayer = target.player

        /* remove ?
        if (player.isInOlympiadMode || targetPlayer.isInOlympiadMode) {
            player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET)
            return false
        }*/

        val events = player.events + target.events
        for (event in events) {
            if (!event.canResurrect(caster, target, false, false)) {
                player.sendPacket(SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skill))
                return false
            }
        }

        if (target.isPlayer) {
            if (!targetPlayer.isDead) {
                canResurrect = false
                if (caster.isPlayer) {
                    val packet = SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skill)
                    caster.sendPacket(packet)
                }
            } else if (targetPlayer.isResurrectionBlocked) {
                canResurrect = false
                if (caster.isPlayer) {
                    caster.sendPacket(SystemMsg.REJECT_RESURRECTION)
                }
            } else {
                val ask = targetPlayer.getAskListener(false)
                val reviveAsk =
                    if (ask != null && ask.value is ReviveAnswerListener) ask.value as ReviveAnswerListener else null
                if (reviveAsk != null) {
                    canResurrect = false
                    if (reviveAsk.isForPet)
                        caster.sendPacket(SystemMsg.WHILE_A_PET_IS_BEING_RESURRECTED_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER) // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
                    else
                        caster.sendPacket(SystemMsg.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED) // Resurrection is already been proposed.
                }
            }
        } else if (target.isPet) {
            if (!target.isDead) {
                canResurrect = false
                if (caster.isPlayer) {
                    val packet = SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(skill)
                    caster.sendPacket(packet)
                }
            } else if (target.isResurrectionBlocked) {
                canResurrect = false
                if (caster.isPlayer) {
                    caster.sendPacket(SystemMsg.REJECT_RESURRECTION)
                }
            } else {
                val ask = targetPlayer.getAskListener(false)
                val reviveAsk =
                    if (ask != null && ask.value is ReviveAnswerListener) ask.value as ReviveAnswerListener else null
                if (reviveAsk != null) {
                    canResurrect = false
                    if (reviveAsk.isForPet)
                        caster.sendPacket(SystemMsg.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED) // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
                    else
                        caster.sendPacket(SystemMsg.A_PET_CANNOT_BE_RESURRECTED_WHILE_ITS_OWNER_IS_IN_THE_PROCESS_OF_RESURRECTING) // Resurrection is already been proposed.
                }
            }
        }

        return canResurrect
    }

}
