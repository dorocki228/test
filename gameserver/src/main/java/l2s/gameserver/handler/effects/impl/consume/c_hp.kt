package l2s.gameserver.handler.effects.impl.consume

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.templates.skill.EffectTemplate
import kotlin.math.min

/**
 * Hp Consume effect implementation.
 */
class c_hp(template: EffectTemplate) : EffectHandler(template) {

    private val _power = params.getDouble("c_hp_param1")

    init {
        ticks = params.getInteger("c_hp_param2")
    }

    override fun consume(abnormal: Abnormal?, target: Creature): Boolean {
        if (target.isDead) {
            return false
        }

        val consume = _power * ticksMultiplier
        val hp = target.currentHp
        val maxHp = target.stat.getMaxRecoverableHp().toDouble()
        if (consume > 0 && hp > maxHp) {
            return false
        }

        if (consume < 0 && hp + consume <= 0) {
            target.sendPacket(SystemMsg.YOUR_SKILL_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_HP)
            return false
        }

        target.currentHp = min(hp + consume, maxHp)

        return true
    }

}