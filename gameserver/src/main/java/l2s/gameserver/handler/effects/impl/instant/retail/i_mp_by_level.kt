package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.commons.math.MathUtils
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.network.l2.components.StatusUpdate
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket
import l2s.gameserver.network.l2.s2c.SystemMessagePacket
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Mana Heal By Level effect implementation.
 *
 * @author UnAfraid
 * @author Java-man
 */
class i_mp_by_level(template: EffectTemplate) : i_abstract_effect(template) {

    private val power: Double = params.getDouble("i_mp_by_level_param1")

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        if (target.isDead || target.isMpBlocked) {
            return
        }

        var addToMp = 0.0
        val levelDiff = target.level - skill.magicLevel - 5
        if (levelDiff <= 9) {
            addToMp = power * (10.0 * (10.0 - MathUtils.constrain(levelDiff, 0, 9)) / 100.0)
            addToMp = target.stat.getValue(DoubleStat.MANA_CHARGE, addToMp)
        }

        val max = target.stat.getMaxRecoverableMp() - target.currentMp
        val healedAmount = MathUtils.constrain(power, 0.0, max)
        target.setCurrentMp(target.currentMp + healedAmount, false)

        val su = StatusUpdate(target, caster, StatusUpdatePacket.UpdateType.REGEN, StatusUpdatePacket.CUR_MP)
        caster.sendPacket(su)
        target.sendPacket(su)
        target.broadcastStatusUpdate()

        val targetPlayer = caster.player
        if (targetPlayer != null) {
            if (caster != target) {
                val packet = SystemMessagePacket(SystemMsg.S2_MP_HAS_BEEN_RESTORED_BY_C1)
                        .addName(caster, targetPlayer)
                        .addInteger(addToMp)
                target.sendPacket(packet)
            } else {
                target.sendPacket(SystemMessagePacket(SystemMsg.S1_MP_HAS_BEEN_RESTORED).addInteger(addToMp))
            }
        }
    }

}
