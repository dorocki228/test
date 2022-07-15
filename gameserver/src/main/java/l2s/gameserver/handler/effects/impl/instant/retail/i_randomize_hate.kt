package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.commons.util.Rnd
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.World
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Randomize Hate effect implementation.
 */
class i_randomize_hate(template: EffectTemplate) : i_abstract_effect(template) {

    private val chance = params.getDouble("i_randomize_hate_param1")

    override fun calcSuccess(caster: Creature, target: Creature, skill: Skill): Boolean {
        if (!target.isMonster) {
            return false
        }

        return Formulas.calcProbability(chance, caster, target, skill)
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetAttackable = target.asMonster() ?: return

        if (caster == targetAttackable) {
            return
        }

        val targetList = World.getAroundCharacters(targetAttackable)
                .filter {
                    if (it == caster) {
                        return@filter false
                    }

                    if (it.isMonster && !it.asMonster().isInFaction(targetAttackable)) {
                        return@filter false
                    }

                    return@filter true
                }
        if (targetList.isEmpty()) {
            return
        }

        // Choosing randomly a new target
        val randomTarget = Rnd.get(targetList)
        val hate = targetAttackable.aggroList.getHate(caster)
        targetAttackable.aggroList.remove(caster, true)
        targetAttackable.aggroList.addDamageHate(randomTarget, 0, hate)
    }
}
