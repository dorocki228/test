package l2s.gameserver.handler.effects.impl.tick

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.Formulas
import l2s.gameserver.stats.StatModifierType
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Dam Over Time effect implementation.
 */
class t_hp_fatal(template: EffectTemplate) : EffectHandler(template) {

    private val _power = params.getDouble("t_hp_fatal_param1")
    private val _mode = params.getEnum("t_hp_fatal_param3", StatModifierType::class.java, true)

    init {
        ticks = params.getInteger("t_hp_fatal_param2")
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

        val damage = _power * ticksMultiplier * 10 // Tests show that 10 times HP DOT is taken during magic critical.
        caster.doAttack(damage, target, skill, true, false, true, false)
    }

    override fun tick(abnormal: Abnormal?, caster: Creature, target: Creature) {
        if (target.isDead) {
            return
        }

        val hp = target.currentHp
        val damage = when (_mode) {
            StatModifierType.DIFF -> {
                _power * ticksMultiplier
            }
            StatModifierType.PER -> {
                hp * _power * ticksMultiplier
            }
        }

        caster.doAttack(damage, target, skill, true, false, false, false)
    }

}