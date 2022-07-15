package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.ThreadPoolManager
import l2s.gameserver.geodata.GeoEngine
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.FlyToLocation
import l2s.gameserver.network.l2.s2c.FlyToLocation.FlyType
import l2s.gameserver.network.l2.s2c.ValidateLocationPacket
import l2s.gameserver.templates.skill.EffectTemplate
import l2s.gameserver.utils.PositionUtils
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.cos
import kotlin.math.sin

/**
 * Teleport To Target effect implementation.
 * @author Didldak, Adry_85
 * @author Java-man
 */
class i_teleport_to_target(template: EffectTemplate?) : i_abstract_effect(template) {

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

        val px = target.x
        val py = target.y
        var ph = PositionUtils.convertHeadingToDegree(target.heading)

        ph += 180.0
        if (ph > 360) {
            ph -= 360.0
        }

        ph = Math.PI * ph / 180
        val x = px + 25 * cos(ph)
        val y = py + 25 * sin(ph)
        val z = target.z

        caster.isUsingFlyingSkill = true

        val loc = GeoEngine.moveCheck(
                caster.x,
                caster.y,
                z,
                x.toInt(),
                y.toInt(),
                caster.geoIndex
        ) ?: caster.loc
        caster.broadcastPacket(FlyToLocation(
                caster,
                loc,
                FlyType.DUMMY
        ))
        caster.stopActions()
        caster.setLoc(loc)
        caster.broadcastPacket(ValidateLocationPacket(caster))

        ThreadPoolManager.getInstance().schedule({
            caster.isUsingFlyingSkill = false
        }, 60, TimeUnit.SECONDS)

    }

}