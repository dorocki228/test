package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Target Me Probability effect implementation.
 * @author Adry_85
 */
class i_target_me(template: EffectTemplate) : i_abstract_effect(template) {

    private val chance = params.getDouble("i_target_me_param1")

    override fun calcSuccess(caster: Creature, target: Creature, skill: Skill): Boolean {
        if (!target.isCreature) {
            return false
        }

        if (!Formulas.calcProbability(chance, caster, target.asCreature(), skill)) {
            return false
        }

        return true
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetPlayable = target.asPlayable() ?: return

        if (targetPlayable.target != caster) {
            targetPlayable.target = caster

            /* need ?
            target.abortCast(true, true)
            target.abortAttack(true, true)

            target.ai.clearNextAction()*/
        }
    }

}