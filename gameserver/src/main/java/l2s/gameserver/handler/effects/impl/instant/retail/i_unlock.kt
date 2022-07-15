package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.commons.util.Rnd
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.model.instances.DoorInstance
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Unlock effect implementation.
 * @author Java-man
 */
class i_unlock(template: EffectTemplate) : i_abstract_effect(template) {

    private val type = when (val param1 = params.getString("i_unlock_param1")) {
        "bypc" -> 0
        "byitem" -> 1
        else -> error("Unknown type $param1")
    }
    private val chanceLevel1 = params.getDouble("i_unlock_param2")
    private val chanceLevel2 = params.getDouble("i_unlock_param3")
    private val chanceLevel3 = params.getDouble("i_unlock_param4")

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        if (!target.isDoor) {
            caster.sendPacket(SystemMsg.INVALID_TARGET)
            return
        }

        val door = target as DoorInstance

        if (door.isOpen) {
            caster.sendPacket(SystemMsg.IT_IS_NOT_LOCKED)
            return
        }

        if (!door.isUnlockable) {
            caster.sendPacket(SystemMsg.THIS_DOOR_CANNOT_BE_UNLOCKED)
            return
        }

        // ключ не подходит к двери
        if (door.key > 0) {
            caster.sendPacket(SystemMsg.THIS_DOOR_CANNOT_BE_UNLOCKED)
            return
        }

        var success = false
        if (type == 0) {
            success = when (door.level) {
                1 -> Rnd.chance(chanceLevel1)
                2 -> Rnd.chance(chanceLevel2)
                3 -> Rnd.chance(chanceLevel3)
                else -> false
            }
        } /*else if (type == 1) {
            for (doorId in _doors.keySet()) {
                if (door.doorId == doorId) {
                    success = Rnd.get(100) < _doors.get(doorId)
                }
            }
        }*/

        if (success) {
            if (type == 1 /*&& door.canBeOpenedbyItem()*/) {
                door.openMe(caster.player, true)
            } else if (type == 0 /*&& door.canBeOpenedBySkill()*/) {
                door.openMe(caster.player, true)
            }
        } else {
            caster.sendPacket(SystemMsg.YOU_HAVE_FAILED_TO_UNLOCK_THE_DOOR)
        }
    }

}