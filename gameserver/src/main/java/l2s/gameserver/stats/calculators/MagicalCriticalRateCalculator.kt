package l2s.gameserver.stats.calculators

import l2s.gameserver.Config
import l2s.gameserver.model.Creature
import l2s.gameserver.model.base.BaseStats
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.item.ItemTemplate
import kotlin.math.max

/**
 * @author UnAfraid
 * @author Java-man
 *
 * @since 12.10.2019
 */
object MagicalCriticalRateCalculator : StatCalculator {

    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        var baseValue = calcWeaponBaseValue(creature, stat)

        if (creature.isPlayer) {
            baseValue += calcEnchantBodyPart(
                creature,
                ItemTemplate.SLOT_LEGS
            )
        }

        baseValue *= BaseStats.WIT.calcBonus(creature)

        baseValue *= 10

        val value = defaultValue(creature, stat, calculationType, baseValue)
        return validateValue(creature, value, 0.0, Config.LIM_MCRIT)
    }

    override fun calcEnchantBodyPartBonus(enchantLevel: Int, isBlessed: Boolean): Double {
        return if (isBlessed) {
            0.5 * max(enchantLevel - 3, 0) + (0.5 * max(enchantLevel - 6, 0))
        } else {
            0.34 * max(enchantLevel - 3, 0) + (0.34 * max(enchantLevel - 6, 0))
        }
    }

}