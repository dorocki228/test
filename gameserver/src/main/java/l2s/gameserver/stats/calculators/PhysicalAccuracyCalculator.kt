package l2s.gameserver.stats.calculators

import l2s.gameserver.model.Creature
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.item.ItemTemplate
import kotlin.math.max
import kotlin.math.sqrt

/**
 * @author UnAfraid
 * @author Java-man
 *
 * @since 12.10.2019
 */
object PhysicalAccuracyCalculator : StatCalculator {

    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        var baseValue = calcEquippedItemsBaseValue(creature, stat)

        // [Square(DEX)] * 5 + lvl + weapon hitbonus;
        val level = creature.level

        baseValue += sqrt(creature.dex.toDouble()) * 5 + level

        if (level > 69) {
            baseValue += level - 69.toDouble()
        }
        if (level > 77) {
            baseValue += 1.0
        }
        if (level > 80) {
            baseValue += 2.0
        }
        if (level > 87) {
            baseValue += 2.0
        }
        if (level > 92) {
            baseValue += 1.0
        }
        if (level > 97) {
            baseValue += 1.0
        }

        if (creature.isPlayer) {
            // Enchanted gloves bonus
            baseValue += calcEnchantBodyPart(creature, ItemTemplate.SLOT_GLOVES)
        }

        return defaultValue(creature, stat, calculationType, baseValue)
    }

    override fun calcEnchantBodyPartBonus(enchantLevel: Int, isBlessed: Boolean): Double {
        return if (isBlessed) {
            0.3 * max(enchantLevel - 3, 0) + 0.3 * max(enchantLevel - 6, 0)
        } else {
            0.2 * max(enchantLevel - 3, 0) + 0.2 * max(enchantLevel - 6, 0)
        }
    }

}