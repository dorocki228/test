package l2s.gameserver.stats.calculators

import l2s.gameserver.model.Creature
import l2s.gameserver.model.base.BaseStats
import l2s.gameserver.stats.DoubleStat

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 12.10.2019
 */
object ShieldDefenceRateCalculator : StatCalculator {

    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        var baseValue = calcEquippedItemsBaseValue(creature, stat)

        baseValue *= BaseStats.CON.calcBonus(creature)

        return defaultValue(creature, stat, calculationType, baseValue)
    }

}