package l2s.gameserver.handler.effects.impl

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.MoveType
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.stats.StatModifierType
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * StatByMoveType effect implementation.
 *
 * @author UnAfraid
 * @author Java-man
 */
abstract class AbstractDoubleStatByMoveType(
        template: EffectTemplate,
        private val stat: DoubleStat
) : EffectHandler(template) {

    private val type = params.getEnum(
            javaClass.simpleName + "_param1",
            MoveType::class.java,
            true
    )
    private val amount = params.getDouble(javaClass.simpleName + "_param2")
    private val modifierType: StatModifierType =
            params.getEnum(
                    javaClass.simpleName + "_param3",
                    StatModifierType::class.java,
                    true
            )

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.stat.mergeMoveTypeValue(stat, type, amount)
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.stat.mergeMoveTypeValue(stat, type, -amount)
    }

}