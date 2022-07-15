package l2s.gameserver.stats.calculators

import l2s.gameserver.model.Creature
import l2s.gameserver.stats.DoubleStat
import kotlin.math.min

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 14.10.2019
 */
object VampiricChanceCalculator : StatCalculator {

    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        val amount = creature.stat.getValue(DoubleStat.ABSORB_DAMAGE_PERCENT, 0.0) * 100
        if (amount <= 0) {
            return 0.0
        }

        val vampiricSum = creature.stat.getVampiricSum()

        val baseValue = min(1.0, vampiricSum / amount / 100.0)

        return defaultValue(creature, stat, calculationType, baseValue)
    }

}