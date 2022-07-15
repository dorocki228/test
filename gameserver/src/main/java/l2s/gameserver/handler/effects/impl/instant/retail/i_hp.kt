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
 * HP change effect. It is mostly used for potions and static damage.
 *
 * @author Nik
 * @author Java-man
 */
class i_hp(template: EffectTemplate) : i_abstract_effect(template) {

    private val amount = params.getDouble("i_hp_param1")
    private val modifierType: StatModifierType =
            params.getEnum(
                    "i_hp_param2",
                    StatModifierType::class.java,
                    true
            )

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        if (targetCreature.isDead || target.isDoor || targetCreature.isHpBlocked) {
            return
        }

        val maxHp = targetCreature.stat.getMaxRecoverableHp() - targetCreature.currentHp
        val amount = when (modifierType) {
            StatModifierType.DIFF -> {
                min(this.amount, maxHp)
            }
            StatModifierType.PER -> {
                min(targetCreature.currentHp * this.amount / 100.0, maxHp)
            }
        }

        if (amount >= 0) {
            if (amount != 0.0) {
                val newHp = amount + targetCreature.currentHp
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
