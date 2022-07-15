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
 * MP change effect. It is mostly used for potions and static damage.
 *
 * @author Nik
 * @author Java-man
 */
class i_mp(template: EffectTemplate) : i_abstract_effect(template) {

    private val amount = params.getDouble("i_mp_param1")
    private val modifierType: StatModifierType =
            params.getEnum(
                    "i_mp_param2",
                    StatModifierType::class.java,
                    true
            )

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        if (targetCreature.isDead || target.isDoor || targetCreature.isMpBlocked) {
            return
        }

        val maxMp = targetCreature.stat.getMaxRecoverableMp() - targetCreature.currentMp
        val amount = when (modifierType) {
            StatModifierType.DIFF -> {
                min(this.amount, maxMp)
            }
            StatModifierType.PER -> {
                min(targetCreature.currentMp * this.amount / 100.0, maxMp)
            }
        }

        if (amount >= 0) {
            if (amount != 0.0) {
                val newMp = amount + targetCreature.currentMp
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
        } else {
            val newMp = max(targetCreature.currentMp - amount, 0.0)
            targetCreature.setCurrentMp(newMp, false)

            val su = StatusUpdate(target, caster, StatusUpdatePacket.UpdateType.DAMAGED, StatusUpdatePacket.CUR_MP)
            caster.sendPacket(su)
            target.sendPacket(su)
            target.broadcastStatusUpdate()
        }
    }

}
