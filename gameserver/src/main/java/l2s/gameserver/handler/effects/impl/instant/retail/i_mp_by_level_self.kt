package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.commons.math.constrain
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.SystemMessagePacket
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Mana Heal By Level Self effect implementation.
 * @author UnAfraid
 * @author Java-man
 */
class i_mp_by_level_self(template: EffectTemplate) : i_abstract_effect(template) {

    private val _power = params.getDouble("i_mp_by_level_self_param1")

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        if (caster.isDead || caster.isMpBlocked) {
            return
        }

        var power = 0.0
        val levelDiff = caster.level - skill.magicLevel
        if (levelDiff <= 9) {
            power = _power * (10 * (10 - levelDiff.constrain(0, 9)) / 100)
        }

        val healedAmount = power.constrain(0.0, caster.stat.getMaxRecoverableMp() - caster.currentMp)
        caster.currentMp = caster.currentMp + healedAmount

        // System message
        val sm = SystemMessagePacket(SystemMsg.S1_MP_HAS_BEEN_RESTORED)
        sm.addInteger(healedAmount)
        caster.sendPacket(sm)
    }

}
