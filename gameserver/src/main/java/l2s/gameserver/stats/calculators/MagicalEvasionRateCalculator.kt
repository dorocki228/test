package l2s.gameserver.stats.calculators

import l2s.gameserver.Config
import l2s.gameserver.model.Creature
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.item.ItemTemplate
import kotlin.math.max
import kotlin.math.sqrt

/**
 * @author UnAfraid
 * @author Java-man
 *
 * @since 13.10.2019
 */
object MagicalEvasionRateCalculator : StatCalculator {

    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        var baseValue = calcEquippedItemsBaseValue(creature, stat)

        if (creature.isPlayer) {
            // Enchanted helm bonus
            baseValue += calcEnchantBodyPart(creature, ItemTemplate.SLOT_HEAD)
        }

        baseValue += sqrt(creature.wit.toDouble()) * 3

        baseValue += creature.level * 2

        val value = defaultValue(creature, stat, calculationType, baseValue)
        return validateValue(creature, value, Double.NEGATIVE_INFINITY, Config.LIM_EVASION)
    }

    override fun calcEnchantBodyPartBonus(enchantLevel: Int, isBlessed: Boolean): Double {
        return if (isBlessed) {
            0.3 * max(enchantLevel - 3, 0) + 0.3 * max(enchantLevel - 6, 0)
        } else {
            0.2 * max(enchantLevel - 3, 0) + 0.2 * max(enchantLevel - 6, 0)
        }
    }

}