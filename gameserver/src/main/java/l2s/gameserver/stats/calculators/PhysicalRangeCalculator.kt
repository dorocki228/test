package l2s.gameserver.stats.calculators

import l2s.gameserver.model.Creature
import l2s.gameserver.stats.DoubleStat

/**
 * @author UnAfraid
 * @author Java-man
 *
 * @since 13.10.2019
 */
object PhysicalRangeCalculator : StatCalculator {

    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        val baseValue = calcWeaponBaseValue(creature, stat)

        return defaultValue(creature, stat, calculationType, baseValue)
    }

}