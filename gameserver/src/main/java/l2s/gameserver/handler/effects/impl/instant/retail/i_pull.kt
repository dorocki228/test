package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.ThreadPoolManager
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.FlyToLocation
import l2s.gameserver.network.l2.s2c.FlyToLocation.FlyType
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * An effect that pulls effected target back to the effector.
 * @author Nik
 * @author Java-man
 */
class i_pull(template: EffectTemplate?) : i_abstract_effect(template) {

    private val speed = params.getInteger("i_pull_param1")
    private val delay = params.getInteger("i_pull_param2")

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

        targetCreature.isUsingFlyingSkill = true

        // In retail, you get debuff, but you are not even moved if there is obstacle. You are still disabled from using skills and moving though.
        val flyLoc = targetCreature.getFlyLocation(
                caster,
                FlyType.WARP_FORWARD,
                false,
                0,
                200
        ) ?: return

        val packet = FlyToLocation(
                targetCreature,
                flyLoc,
                FlyType.WARP_FORWARD,
                speed,
                delay,
                0
        )
        targetCreature.broadcastPacket(packet)
        targetCreature.setLoc(flyLoc)

        ThreadPoolManager.getInstance().schedule({
            targetCreature.isUsingFlyingSkill = false
        }, 60, TimeUnit.SECONDS)
    }

}