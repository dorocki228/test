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
import kotlin.math.min

/**
 * @author UnAfraid
 * @author Java-man
 */
class i_cp(template: EffectTemplate) : i_abstract_effect(template) {

    private val amount = params.getDouble("i_cp_param1")
    private val modifierType: StatModifierType =
            params.getEnum(
                    "i_cp_param2",
                    StatModifierType::class.java,
                    true
            )

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        if (targetCreature.isDead || target.isDoor || targetCreature.isHpBlocked) {
            return
        }

        val maxCp = targetCreature.stat.getMaxRecoverableCp() - targetCreature.currentCp
        val amount = when (modifierType) {
            StatModifierType.DIFF -> {
                min(this.amount, maxCp)
            }
            StatModifierType.PER -> {
                min(targetCreature.currentCp * this.amount / 100.0, maxCp)
            }
        }

        if (amount != 0.0) {
            val newCp = amount + targetCreature.currentCp
            targetCreature.setCurrentCp(newCp, false)
            // TODO targetCreature.broadcastStatusUpdate(caster)

            val su = StatusUpdate(target, caster, StatusUpdatePacket.UpdateType.REGEN, StatusUpdatePacket.CUR_CP)
            caster.sendPacket(su)
            target.sendPacket(su)
            target.broadcastStatusUpdate()
        }

        if (amount >= 0) {
            val sm: SystemMessagePacket
            if (caster != targetCreature) {
                sm = SystemMessagePacket(SystemMsg.S2_CP_HAS_BEEN_RESTORED_BY_C1)
                sm.addName(caster)
            } else {
                sm = SystemMessagePacket(SystemMsg.S1_CP_HAS_BEEN_RESTORED)
            }
            sm.addInteger(amount)
            targetCreature.sendPacket(sm)
        }
    }

}
