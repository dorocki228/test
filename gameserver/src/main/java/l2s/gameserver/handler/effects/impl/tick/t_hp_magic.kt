package l2s.gameserver.handler.effects.impl.tick

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import kotlin.math.abs

/**
 * MagicalAttack-damage over time effect implementation.
 * @author Nik
 */
class t_hp_magic(template: EffectTemplate) : EffectHandler(template) {

    private val _power = params.getDouble("t_hp_magic_param1")

    init {
        ticks = params.getInteger("t_hp_magic_param2")
    }

    override fun tick(abnormal: Abnormal?, caster: Creature, target: Creature) {
        if (target.isDead) {
            return
        }

        var damage = Formulas.calcMagicDam(
                caster,
                target,
                skill,
                caster.stat.getMAtk(),
                abs(_power),
                target.stat.getMDef(),
                false,
                false,
                false
        ) // In retail spiritshots change nothing.

        damage *= ticksMultiplier

        val hp = target.currentHp
        if (damage >= hp - 1) {
            damage = hp - 1
        }

        caster.doAttack(damage, target, skill, true, false, false, false)
    }
}