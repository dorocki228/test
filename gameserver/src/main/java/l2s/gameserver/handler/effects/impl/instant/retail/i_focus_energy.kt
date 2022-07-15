package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.EtcStatusUpdatePacket
import l2s.gameserver.network.l2.s2c.SystemMessagePacket
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

/**
 * Focus Energy effect implementation.
 *
 * @author DS
 * @author Java-man
 */
class i_focus_energy(template: EffectTemplate) : i_abstract_effect(template) {

    private val maxCharges = params.getInteger("i_focus_energy_param1")

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetPlayer = target.player ?: return

        val currentCharges = targetPlayer.charges
        val value = targetPlayer.stat.getValue(DoubleStat.MAX_MOMENTUM, 0.0)
        val maxCharges = min(maxCharges, value.toInt())
        if (currentCharges >= maxCharges) {
            if (!skill.isTrigger) {
                targetPlayer.sendPacket(SystemMsg.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY_)
            }
            return
        }

        val newCharge = min(currentCharges + 1, maxCharges)

        targetPlayer.charges = newCharge

        if (newCharge == maxCharges) {
            targetPlayer.sendPacket(SystemMsg.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY_)
        } else {
            val sm = SystemMessagePacket(SystemMsg.YOUR_FORCE_HAS_INCREASED_TO_LEVEL_S1)
            sm.addInteger(newCharge.toDouble())
            targetPlayer.sendPacket(sm)
        }

        targetPlayer.sendPacket(EtcStatusUpdatePacket(targetPlayer))
    }

}