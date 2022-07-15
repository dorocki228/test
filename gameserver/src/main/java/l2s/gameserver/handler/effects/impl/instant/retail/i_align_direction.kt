package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.network.l2.s2c.FinishRotating
import l2s.gameserver.network.l2.s2c.StartRotatingPacket
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Bluff effect implementation.
 *
 * @author decad
 * @author Java-man
 */
class i_align_direction(template: EffectTemplate) : i_abstract_effect(template) {

    private val chance = params.getInteger("i_align_direction_param1")

    override fun calcSuccess(caster: Creature, target: Creature, skill: Skill): Boolean {
        if (!target.isCreature) {
            return false
        }

        return Formulas.calcProbability(chance.toDouble(), caster, target, skill)
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature: Creature = target.asCreature() ?: return

        // Headquarters NPC should not rotate
        if (targetCreature.npcId == 35062 || targetCreature.isRaid || targetCreature.isRaidMinion) {
            return
        }

        val startPacket = StartRotatingPacket(targetCreature, targetCreature.heading, 1, 65535)
        targetCreature.broadcastPacket(startPacket)
        val finishRotating = FinishRotating(targetCreature, caster.heading, 65535)
        targetCreature.broadcastPacket(finishRotating)
        targetCreature.heading = caster.heading
    }

}