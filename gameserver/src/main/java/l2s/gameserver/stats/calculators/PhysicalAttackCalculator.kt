package l2s.gameserver.stats.calculators

import l2s.gameserver.model.Creature
import l2s.gameserver.model.base.BaseStats
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.item.ItemTemplate
import kotlin.math.max

/**
 * @author UnAfraid
 * @author Java-man
 *
 * @since 09.10.2019
 */
object PhysicalAttackCalculator : StatCalculator {

    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        var baseValue = calcWeaponBaseValue(creature, stat)

        baseValue += calcEnchantedItemBonus(creature, stat)

        if (creature.isPlayer) {
            baseValue += calcEnchantBodyPart(
                creature,
                ItemTemplate.SLOT_CHEST,
                ItemTemplate.SLOT_FULL_ARMOR
            )
        }

        baseValue *= BaseStats.STR.calcBonus(creature)

        baseValue *= creature.levelBonus

        return defaultValue(creature, stat, calculationType, baseValue)
    }

    override fun calcEnchantBodyPartBonus(enchantLevel: Int, isBlessed: Boolean): Double {
        return if (isBlessed) {
            (3 * max(enchantLevel - 3, 0) + 3 * max(enchantLevel - 6, 0)).toDouble()
        } else {
            (2 * max(enchantLevel - 3, 0) + 2 * max(enchantLevel - 6, 0)).toDouble()
        }
    }

}