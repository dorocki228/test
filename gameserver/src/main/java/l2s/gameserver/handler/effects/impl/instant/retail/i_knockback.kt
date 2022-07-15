package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.ThreadPoolManager
import l2s.gameserver.ai.CtrlEvent
import l2s.gameserver.geodata.GeoEngine
import l2s.gameserver.geometry.Location
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.FlyToLocation
import l2s.gameserver.network.l2.s2c.FlyToLocation.FlyType
import l2s.gameserver.network.l2.s2c.ValidateLocationPacket
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin


/**
 * Check if this effect is not counted as being stunned.
 * @author UnAfraid
 * @author Java-man
 */
class i_knockback(template: EffectTemplate?) : i_abstract_effect(template) {

    private val distance = params.getInteger("i_knockback_param1")
    private val speed = params.getInteger("i_knockback_param2")
    private val _knockDown = params.getInteger("i_knockback_param4") == 1
    private val flyType = when {
        _knockDown -> FlyType.PUSH_DOWN_HORIZONTAL
        else -> FlyType.PUSH_HORIZONTAL
    }

    override fun calcSuccess(caster: Creature, target: Creature, skill: Skill): Boolean {
        if (!target.isCreature) {
            return false
        }

        return Formulas.calcProbability(Double.NaN, caster, target, skill)
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        // TODO move conditions before skill usage
        if (targetCreature.isThrowAndKnockImmune) {
            targetCreature.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET)
            return
        }

        // Тычок/отброс нельзя наложить на осадных саммонов
        val npc = targetCreature.asNpc()
        if (npc != null && npc.template.race == 21) {
            caster.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET)
            return
        }

        /* need ?
        if (targetCreature.isInPeaceZone) {
            caster.sendPacket(SystemMsg.YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE)
            return
        }*/

        /*if (_knockDown) {
            target.ai.notifyEvent(CtrlEvent.EVT_KNOCK_DOWN, target)
        } else {
            target.ai.notifyEvent(CtrlEvent.EVT_KNOCK_BACK, target)
        }
        knockBack(caster, targetCreature)*/

        val curX = target.x
        val curY = target.y
        val curZ = target.z

        val dx = (caster.x - curX).toDouble()
        val dy = (caster.y - curY).toDouble()
        val dz = (caster.z - curZ).toDouble()
        val distance = Math.sqrt(dx * dx + dy * dy)

        if (distance > 2000.0) return

        var offset = Math.min(distance.toInt() + 75, 1400)
        offset = (offset + Math.abs(dz)).toInt()

        if (offset < 5) offset = 5

        if (distance < 1.0) return

        val sin = dy / distance
        val cos = dx / distance

        var x = caster.x - (offset * cos).toInt()
        var y = caster.y - (offset * sin).toInt()
        val z = target.z

        val destiny = GeoEngine.moveCheck(target.x, target.y, target.z, x, y, target.geoIndex) ?: return

        x = destiny.getX()
        y = destiny.getY()

        if (target.flags.knockDowned.start(this)) {
            target.abortAttack(true, true)
            target.abortCast(true, true)
            target.movement.stopMove()
            target.ai.notifyEvent(CtrlEvent.EVT_KNOCK_DOWN, target)
            target.broadcastPacket(FlyToLocation(target, Location(x, y, z), FlyType.PUSH_DOWN_HORIZONTAL, 0, 0, 0))
            target.setXYZ(x, y, z)
        }

        ThreadPoolManager.getInstance().schedule({
            if (target.flags.knockDowned.stop(this)) {
                if (!target.isPlayer)
                    target.ai.notifyEvent(CtrlEvent.EVT_THINK)
            }
        }, skill.abnormalTime.toLong(), TimeUnit.SECONDS)
    }

    /*override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        //target.stopActions();
        if (_knockDown) {
            target.ai.notifyEvent(CtrlEvent.EVT_KNOCK_DOWN, target)
            knockBack(caster, target)
        } else {
            target.ai.notifyEvent(CtrlEvent.EVT_KNOCK_BACK, target)
        }
    }*/

    private fun knockBack(caster: Creature, target: Creature) {
        val radians = Math.toRadians(caster.calculateAngleTo(target))
        val x = target.x + distance * cos(radians)
        val y = target.y + distance * sin(radians)
        val z = target.z
        val loc = GeoEngine.moveCheck(
                target.x,
                target.y,
                z,
                x.toInt(),
                y.toInt(),
                target.geoIndex
        ) ?: caster.loc

        target.flags.knockDowned.start(this)

        val packet = FlyToLocation(
                target,
                loc,
                flyType,
                speed,
                0,
                0
        )
        target.broadcastPacket(packet)

        target.setLoc(loc)
        if (_knockDown) {
            target.heading = target.calculateHeadingTo(caster)
        }

        target.broadcastPacket(ValidateLocationPacket(target))
        target.ai.notifyEvent(CtrlEvent.EVT_THINK)
    }

}