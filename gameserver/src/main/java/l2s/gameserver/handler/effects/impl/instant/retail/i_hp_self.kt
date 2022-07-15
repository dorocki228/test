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
 * Hp By Level Self effect implementation.
 * @author Sdw
 * @author Java-man
 */
class i_hp_self(template: EffectTemplate) : i_abstract_effect(template) {

    private val _power = params.getDouble("i_hp_self_param1")

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        if (caster.isDead || caster.isHpBlocked) {
            return
        }

        val healedAmount = _power.constrain(0.0, caster.stat.getMaxRecoverableHp() - caster.currentHp)
        caster.currentHp = caster.currentHp + healedAmount

        // System message
        val sm = SystemMessagePacket(SystemMsg.S1_HP_HAS_BEEN_RESTORED)
        sm.addInteger(healedAmount)
        caster.sendPacket(sm)
    }

}
