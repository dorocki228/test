package l2s.gameserver.stats.calculators

import l2s.gameserver.model.Creature
import l2s.gameserver.model.base.BaseStats
import l2s.gameserver.stats.DoubleStat

/**
 * @author Java-man
 * @since 13.10.2019
 */
object BreathCalculator : StatCalculator {

    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        var baseValue = creature.template.getBaseValue(DoubleStat.BREATH)
            .orElse(0.0)

        baseValue *= BaseStats.CON.calcBonus(creature)

        return defaultValue(creature, stat, calculationType, baseValue)
    }

}