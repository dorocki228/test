package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.commons.util.Rnd
import l2s.gameserver.ThreadPoolManager
import l2s.gameserver.ai.CtrlIntention
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Confuse effect implementation.
 * @author littlecrow
 * @author Java-man
 */
class i_confuse(template: EffectTemplate) : i_abstract_effect(template) {

    private val chance = params.getDouble("i_confuse_param1")
    private val time = params.getLong("i_confuse_param2")

    override fun calcSuccess(caster: Creature, target: Creature, skill: Skill): Boolean {
        if (!target.isCreature) {
            return false
        }

        return Formulas.calcProbability(chance, caster, target, skill)
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature: Creature = target.asCreature() ?: return

        targetCreature.flags.confused.start()

        ConfusionTask(caster, targetCreature, time)
    }

    companion object {
        private class ConfusionTask(
                private val caster: Creature,
                private val target: Creature,
                private var time: Long
        ) : Runnable {

            private val future = ThreadPoolManager.getInstance()
                    .scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS)

            override fun run() {
                if (target.isDead) {
                    if (target.flags.confused.stop()) {
                        target.abortAttack(true, false)
                        target.abortCast(true, false)
                        target.movement.stopMove()
                        target.ai.attackTarget = null
                        target.setWalking()
                        target.ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE)
                    }
                    future.cancel(false)
                    return
                }

                if (time == 0L) {
                    if (target.flags.confused.stop()) {
                        target.abortAttack(true, false)
                        target.abortCast(true, false)
                        target.movement.stopMove()
                        target.ai.attackTarget = null
                        target.setWalking()
                        target.ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE)
                    }
                    future.cancel(false)
                    return
                }

                time--

                attackRandomCreature(caster, target)
            }
        }

        fun attackRandomCreature(caster: Creature?, target: Creature) {
            val chars = if (caster != null) {
                target.getAroundCharacters(2000, 300)
                        .filter { it != caster }
            } else {
                target.getAroundCharacters(2000, 300)
            }

            if (chars.isEmpty()) {
                return
            }

            val randomChar = Rnd.get<Creature>(chars)
            target.target = randomChar
            target.ai.Attack(randomChar, true, false)
        }
    }

}