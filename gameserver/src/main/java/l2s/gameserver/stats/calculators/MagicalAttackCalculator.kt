package l2s.gameserver.stats.calculators

import l2s.gameserver.model.Creature
import l2s.gameserver.model.base.BaseStats
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.item.ItemTemplate
import kotlin.math.max
import kotlin.math.pow

/**
 * @author UnAfraid
 * @author Java-man
 *
 * @since 09.10.2019
 */
object MagicalAttackCalculator : StatCalculator {

    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        var baseValue = calcWeaponBaseValue(creature, stat)

        baseValue = creature.stat.getValue(DoubleStat.MAGICAL_ATTACK_ADD, baseValue)

        baseValue += calcEnchantedItemBonus(creature, stat)

        if (creature.isPlayer) {
            baseValue += calcEnchantBodyPart(
                creature,
                ItemTemplate.SLOT_CHEST,
                ItemTemplate.SLOT_FULL_ARMOR
            )
        }

        baseValue *= BaseStats.INT.calcBonus(creature).pow(2)

        baseValue *= creature.levelBonus.pow(2)

        return defaultValue(creature, stat, calculationType, baseValue)
    }

    override fun calcEnchantBodyPartBonus(enchantLevel: Int, isBlessed: Boolean): Double {
        return if (isBlessed) {
            (2 * max(enchantLevel - 3, 0) + 2 * max(enchantLevel - 6, 0)).toDouble()
        } else {
            (1.4 * max(enchantLevel - 3, 0) + 1.4 * max(enchantLevel - 6, 0)).toDouble()
        }
    }

}