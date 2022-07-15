package l2s.gameserver.handler.effects.impl.consume

import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate
import kotlin.math.min


/**
 * Chameleon Rest effect implementation.
 */
class c_chameleon_rest(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.RELAX) {

    private val _power = params.getDouble("c_chameleon_rest_param1")

    init {
        ticks = params.getInteger("c_chameleon_rest_param2")
    }

    override fun consume(abnormal: Abnormal?, target: Creature): Boolean {
        if (target.isDead) {
            return false
        }

        if (!target.isSitting) {
            return false
        }

        /* remove ?
        if (target.isCurrentHpFull) {
            target.sendPacket(SystemMsg.THAT_SKILL_HAS_BEEN_DEACTIVATED_AS_HP_WAS_FULLY_RECOVERED)
            return false
        }*/

        val consume = _power * ticksMultiplier
        val mp = target.currentMp
        val maxMp = target.stat.getMaxRecoverableMp().toDouble()

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

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        val player = target.player
        if (player.movement.isMoving)
            player.movement.stopMove()
        player.sitDown(null)
    }

}