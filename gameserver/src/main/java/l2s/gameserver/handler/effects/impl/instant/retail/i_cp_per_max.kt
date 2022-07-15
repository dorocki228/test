package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.network.l2.components.StatusUpdate
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket
import l2s.gameserver.network.l2.s2c.SystemMessagePacket
import l2s.gameserver.stats.StatModifierType
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min

/**
 * Cp Heal Percent effect implementation.
 * @author UnAfraid
 * @author Java-man
 */
class i_cp_per_max(template: EffectTemplate) : i_abstract_effect(template) {

    private val amount = params.getDouble("i_cp_per_max_param1")
    private val modifierType: StatModifierType =
            params.getEnum(
                    "i_cp_per_max_param2",
                    StatModifierType::class.java,
                    true
            )

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        if (targetCreature.isDead || target.isDoor || targetCreature.isHpBlocked) {
            return
        }

        val full = amount == 100.0

        var power = when {
            full -> targetCreature.maxCp.toDouble()
            else -> targetCreature.maxCp * amount / 100.0
        }
        // Prevents overheal and negative amount
        power = max(min(power, targetCreature.stat.getMaxRecoverableCp() - targetCreature.currentCp), 0.0)
        if (power != 0.0) {
            val newCp = power + targetCreature.currentCp
            targetCreature.setCurrentCp(newCp, false)
            // TODO targetCreature.broadcastStatusUpdate(caster)

            val su = StatusUpdate(target, caster, StatusUpdatePacket.UpdateType.REGEN, StatusUpdatePacket.CUR_CP)
            caster.sendPacket(su)
            target.sendPacket(su)
            target.broadcastStatusUpdate()
        }

        val sm = SystemMessagePacket(SystemMsg.S1_CP_HAS_BEEN_RESTORED)
        sm.addInteger(power)
        targetCreature.sendPacket(sm)
    }

}
