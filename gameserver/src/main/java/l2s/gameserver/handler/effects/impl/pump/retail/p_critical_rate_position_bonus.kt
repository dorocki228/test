package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.commons.math.MathUtils
import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.stats.StatModifierType
import l2s.gameserver.templates.skill.EffectTemplate
import l2s.gameserver.utils.PositionUtils.Position

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 16.10.2019
 */
class p_critical_rate_position_bonus(template: EffectTemplate) : EffectHandler(template) {

    private val position = params.getEnum(
            "p_critical_rate_position_bonus_param1",
            Position::class.java,
            true
    )
    private val amount = params.getDouble("p_critical_rate_position_bonus_param2")
    private val modifierType =
            params.getEnum(
                    "p_critical_rate_position_bonus_param3",
                    StatModifierType::class.java,
                    true
            )

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        val value = amount / 100.0 + 1
        target.stat.mergePositionTypeValue(
                DoubleStat.CRITICAL_RATE,
                position,
                value,
                MathUtils::mul
        )
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        val value = amount / 100.0 + 1
        target.stat.mergePositionTypeValue(
                DoubleStat.CRITICAL_RATE,
                position,
                value,
                MathUtils::div
        )
    }

}