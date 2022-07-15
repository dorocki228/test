package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.ai.CtrlIntention
import l2s.gameserver.geometry.Location
import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate
import l2s.gameserver.utils.PositionUtils
import kotlin.math.cos
import kotlin.math.sin

/**
 * Fear effect implementation.
 * @author littlecrow
 * @author Java-man
 */
class p_fear(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.FEAR) {

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        if (target.isFearImmune) {
            return false
        }

        // Fear нельзя наложить на осадных саммонов
        val npc = target.asNpc()
        if (npc != null && npc.template.race == 21) {
            return false
        }

        /* need ?
        if (target.isInPeaceZone) {
            return false
        }*/

        return true
    }

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.abortAttack(true, true)
        target.abortCast(true, true)
        target.movement.stopMove()

        runInFear(caster, target)
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.ai.intention = CtrlIntention.AI_INTENTION_ACTIVE
    }

    companion object {
        private const val FEAR_RANGE = 500

        fun runInFear(caster: Creature?, target: Creature) {
            val heading = target.heading
            val radians = Math.toRadians(caster?.calculateAngleTo(target)
                    ?: PositionUtils.convertHeadingToDegree(heading))

            val x = target.x
            val y = target.y

            val posX = x + (FEAR_RANGE * cos(radians)).toInt()
            val posY = y + (FEAR_RANGE * sin(radians)).toInt()
            val posZ = target.z

            val destination = Location.findAroundPosition(posX, posY, posZ, 0, 300, target.geoIndex)

            target.setRunning()
            target.movement.moveToLocation(destination, 0, false)
        }

    }

}