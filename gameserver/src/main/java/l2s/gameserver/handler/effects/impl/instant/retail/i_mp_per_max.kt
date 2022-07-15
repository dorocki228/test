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
 * Mana Heal Percent effect implementation.
 * @author UnAfraid
 * @author Java-man
 */
class i_mp_per_max(template: EffectTemplate) : i_abstract_effect(template) {

    private val amount = params.getDouble("i_mp_per_max_param1")

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        if (targetCreature.isDead || target.isDoor || targetCreature.isMpBlocked) {
            return
        }

        val full = amount == 100.0

        var power = when {
            full -> targetCreature.maxMp.toDouble()
            else -> targetCreature.maxMp * amount / 100.0
        }
        // Prevents overheal and negative amount
        power = max(min(power, targetCreature.stat.getMaxRecoverableMp() - targetCreature.currentMp), 0.0)

        if (power != 0.0) {
            val newMp = power + targetCreature.currentMp
            targetCreature.setCurrentMp(newMp, false)
            // TODO targetCreature.broadcastStatusUpdate(caster)

            val su = StatusUpdate(target, caster, StatusUpdatePacket.UpdateType.REGEN, StatusUpdatePacket.CUR_MP)
            caster.sendPacket(su)
            target.sendPacket(su)
            target.broadcastStatusUpdate()
        }

        val sm: SystemMessagePacket
        if (caster != targetCreature) {
            sm = SystemMessagePacket(SystemMsg.S2_MP_HAS_BEEN_RESTORED_BY_C1)
            sm.addName(caster)
        } else {
            sm = SystemMessagePacket(SystemMsg.S1_MP_HAS_BEEN_RESTORED)
        }
        sm.addInteger(amount)
        targetCreature.sendPacket(sm)
    }

}
