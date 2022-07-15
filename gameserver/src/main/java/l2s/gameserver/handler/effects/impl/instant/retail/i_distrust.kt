package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.commons.util.Rnd
import l2s.gameserver.ThreadPoolManager
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.model.instances.NpcInstance
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Distrust effect implementation.
 * @author Java-man
 */
class i_distrust(template: EffectTemplate) : i_abstract_effect(template) {

    private val chance = params.getInteger("i_distrust_param1")
    private val time = params.getLong("i_distrust_param2")

    override fun calcSuccess(caster: Creature, target: Creature, skill: Skill): Boolean {
        if (!target.isMonster) {
            return false
        }

        if (!Formulas.calcMagicSuccess(caster, target.asMonster(), skill)) {
            return false
        }

        return true
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetMonster = target.asMonster() ?: return

        var lvlDiff = (skill.magicLevel - targetMonster.level).toDouble()
        if (lvlDiff > 4) {
            lvlDiff /= 4.0
            lvlDiff *= chance
            if (!Rnd.chance(lvlDiff)) {
                return
            }
        } else if (!Rnd.chance(chance)) {
            return
        }

        for (t in targetMonster.getAroundNpc(1000, 300)) {
            if (!t.isMonster) {
                continue
            }

            targetMonster.aggroList.addDamageHate(t, 0, HATE)
            // targetMonster.startConfused()
            val task = StopDistrust(targetMonster, t)
            ThreadPoolManager.getInstance().schedule(task, time, TimeUnit.SECONDS)
            break
        }
    }

    companion object {
        private const val HATE = 100000

        private class StopDistrust(
                private val mob: NpcInstance,
                private val target: NpcInstance
        ) : Runnable {

            override fun run() {
                if (!target.isDead) {
                    mob.aggroList.reduceHate(target, HATE)
                }
                // mob.stopConfused()
            }
        }
    }

}