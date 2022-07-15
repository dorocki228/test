package l2s.gameserver.stats.calculators

import l2s.gameserver.model.Creature
import l2s.gameserver.stats.DoubleStat

/**
 * @author Java-man
 * @since 01.10.2019
 */
object DefaultCalculator : StatCalculator {
    
    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        return defaultValue(creature, stat, calculationType, initValue)
    }

}