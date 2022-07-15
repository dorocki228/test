package l2s.gameserver.handler.effects.impl.tick

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.StatModifierType
import l2s.gameserver.templates.skill.EffectTemplate
import kotlin.math.min

/**
 * Mana Heal Over Time effect implementation.
 */
class t_mp(template: EffectTemplate) : EffectHandler(template) {

    private val _power = params.getDouble("t_mp_param1")
    private val _mode = params.getEnum("t_mp_param3", StatModifierType::class.java, true)

    init {
        ticks = params.getInteger("t_mp_param2")
    }

    override fun tick(abnormal: Abnormal?, caster: Creature, target: Creature) {
        if (target.isDead) {
            return
        }

        val mp = target.currentMp
        val maxMp = target.stat.getMaxRecoverableMp().toDouble()
        var power = when (_mode) {
            StatModifierType.DIFF -> {
                _power * ticksMultiplier
            }
            StatModifierType.PER -> {
                mp * _power * ticksMultiplier
            }
        }

        if (power > 0 && mp > maxMp) {
            return
        }

        if (power < 0 && mp + power <= 0) {
            power = -mp
        }

        target.currentMp = min(target.currentMp + power, maxMp)
    }
}