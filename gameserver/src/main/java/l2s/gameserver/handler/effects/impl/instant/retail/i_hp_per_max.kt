package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.network.l2.components.StatusUpdate
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket
import l2s.gameserver.network.l2.s2c.SystemMessagePacket
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min

/**
 * Heal Percent effect implementation.
 * @author UnAfraid
 * @author Java-man
 */
class i_hp_per_max(template: EffectTemplate) : i_abstract_effect(template) {

    private val amount = params.getDouble("i_hp_per_max_param1")

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        if (targetCreature.isDead || target.isDoor || targetCreature.isHpBlocked) {
            return
        }

        val full = amount == 100.0

        var power = when {
            full -> targetCreature.maxHp.toDouble()
            else -> targetCreature.maxHp * amount / 100.0
        }
        // Prevents overheal and negative amount
        power = max(min(power, targetCreature.stat.getMaxRecoverableHp() - targetCreature.currentHp), 0.0)
        if (power >= 0) {
            if (power != 0.0) {
                val newHp = power + targetCreature.currentHp
                targetCreature.setCurrentHp(newHp, false, false)
                // TODO targetCreature.broadcastStatusUpdate(caster)

                val su = StatusUpdate(target, caster, StatusUpdatePacket.UpdateType.REGEN, StatusUpdatePacket.CUR_HP)
                caster.sendPacket(su)
                target.sendPacket(su)
                target.broadcastStatusUpdate()
            }

            val sm: SystemMessagePacket
            if (caster != targetCreature) {
                sm = SystemMessagePacket(SystemMsg.S2_HP_HAS_BEEN_RESTORED_BY_C1)
                sm.addName(caster)
            } else {
                sm = SystemMessagePacket(SystemMsg.S1_HP_HAS_BEEN_RESTORED)
            }
            sm.addInteger(amount)
            targetCreature.sendPacket(sm)
        } else {
            targetCreature.reduceCurrentHp(-amount, caster, skill, true, true,
                    false, false, false,
                    false, true)
        }
    }

}
