package l2s.gameserver.stats.calculators

import l2s.gameserver.Config
import l2s.gameserver.model.Creature
import l2s.gameserver.model.base.BaseStats
import l2s.gameserver.stats.DoubleStat
import kotlin.math.max

/**
 * @author UnAfraid
 * @author Java-man
 *
 * @since 10.10.2019
 */
object PhysicalAttackSpeedCalculator : StatCalculator {

    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        var baseValue = calcWeaponBaseValue(creature, stat)

        baseValue *= BaseStats.DEX.calcBonus(creature)

        val value = getValue(creature, stat, calculationType, baseValue)
        return validateValue(creature, value, 1.0, Config.LIM_PATK_SPD)
    }

    fun getValue(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            baseValue: Double
    ): Double {
        val passiveMul = max(creature.stat.getPassiveMul(stat), 0.7)
        val passiveAdd = creature.stat.getPassiveAdd(stat)

        return if (calculationType == CalculationType.FULL_VALUE) {
            val mul = max(creature.stat.getMul(stat), 0.7)
            val add = creature.stat.getAdd(stat)
            val moveTypeValue = creature.stat.getMoveTypeValue(stat, creature.moveType)
            (baseValue * passiveMul + passiveAdd) * mul + add + moveTypeValue
        } else {
            baseValue * passiveMul + passiveAdd
        }
    }

}