package l2s.gameserver.handler.effects.impl.consume

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.templates.skill.EffectTemplate
import kotlin.math.min

/**
 * Mp Consume Per Level effect implementation.
 */
class c_mp_by_level(template: EffectTemplate) : EffectHandler(template) {

    private val _power = params.getDouble("c_mp_by_level_param1")

    init {
        ticks = params.getInteger("c_mp_by_level_param2")
    }

    override fun consume(abnormal: Abnormal?, target: Creature): Boolean {
        if (target.isDead) {
            return false
        }

        val mp = target.currentMp
        val maxMp = target.stat.getMaxRecoverableMp().toDouble()
        val consume = _power * ticksMultiplier * ((target.level - 1) / 7.5)
        if (consume > 0 && mp > maxMp) {
            return false
        }

        if (consume < 0 && mp + consume <= 0) {
            target.sendPacket(SystemMsg.YOUR_SKILL_WAS_DEACTIVATED_DUE_TO_LACK_OF_MP)
            return false
        }

        target.currentMp = min(mp + consume, maxMp)

        return true
    }

}