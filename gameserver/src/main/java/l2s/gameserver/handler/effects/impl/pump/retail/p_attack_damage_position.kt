package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.commons.math.MathUtils
import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate
import l2s.gameserver.utils.PositionUtils.Position

/**
 * @author Nik
 * @author Java-man
 *
 * @since 16.10.2019
 */
class p_attack_damage_position(template: EffectTemplate) : EffectHandler(template) {

    private val amount = params.getDouble("amount")
    private val position = params.getEnum("position", Position::class.java)

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        val value = amount / 100.0 + 1
        target.stat.mergePositionTypeValue(
                DoubleStat.ATTACK_DAMAGE,
                position,
                value,
                MathUtils::mul
        )
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        val value = amount / 100.0 + 1
        target.stat.mergePositionTypeValue(
                DoubleStat.ATTACK_DAMAGE,
                position,
                value,
                MathUtils::div
        )
    }

}