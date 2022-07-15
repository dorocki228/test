package l2s.gameserver.stats.calculators

import l2s.gameserver.model.Creature
import l2s.gameserver.stats.DoubleStat

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 10.10.2019
 */
object ShieldDefenceCalculator : StatCalculator {

    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        val baseValue = calcEquippedItemsBaseValue(creature, stat)
        return defaultValue(creature, stat, calculationType, baseValue)
    }

}