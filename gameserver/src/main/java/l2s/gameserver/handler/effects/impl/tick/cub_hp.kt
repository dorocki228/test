package l2s.gameserver.handler.effects.impl.tick

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.Formulas
import l2s.gameserver.stats.StatModifierType
import l2s.gameserver.templates.skill.EffectTemplate
import kotlin.math.abs
import kotlin.math.min

/**
 * Cub Dam Over Time effect implementation.
 */
class cub_hp(template: EffectTemplate) : EffectHandler(template) {

    private val _power = params.getDouble("cub_hp_param1")
    private val _mode = params.getEnum("cub_hp_param3", StatModifierType::class.java, true)

    init {
        ticks = params.getInteger("cub_hp_param2")
    }

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        if (!skill.isMagic) {
            return
        }

        // TODO: M.Crit can occur even if this skill is resisted. Only then m.crit damage is applied and not debuff
        val mcrit = Formulas.calcCrit(skill.magicCriticalRate, caster, target, skill)
        if (!mcrit) {
            return
        }

        var damage = _power * ticksMultiplier * 10 // Tests show that 10 times HP DOT is taken during magic critical.
        if (damage < 0) {
            damage = abs(damage)
            if (damage >= target.currentHp - 1) {
                damage = target.currentHp - 1
            }

            caster.doAttack(damage, target, skill, true, false, true, false)
        } else {
            val maxHp = target.stat.getMaxRecoverableHp().toDouble()
            if (target.currentHp > maxHp) {
                return
            }

            target.currentHp = min(target.currentHp + damage, maxHp)
        }
    }

    override fun tick(abnormal: Abnormal?, caster: Creature, target: Creature) {
        if (target.isDead) {
            return
        }

        val hp = target.currentHp
        var damage = when (_mode) {
            StatModifierType.DIFF -> {
                _power * ticksMultiplier
            }
            StatModifierType.PER -> {
                hp * _power * ticksMultiplier
            }
        }

        if (damage < 0) {
            damage = abs(damage)
            if (damage >= hp - 1) {
                damage = hp - 1
            }

            caster.doAttack(damage, target, skill, true, false, false, false)
        } else {
            val maxHp = target.stat.getMaxRecoverableHp().toDouble()
            if (hp > maxHp) {
                return
            }

            target.currentHp = min(hp + damage, maxHp)
        }
    }

}