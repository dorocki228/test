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
 * @since 13.10.2019
 */
object SpeedCalculator : StatCalculator {
    
    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        var baseValue: Double = getBaseSpeed(creature, stat)

        if (creature.isPlayer) {
            // Enchanted feet bonus
            baseValue += calcEnchantBodyPart(creature, ItemTemplate.SLOT_FEET)
        }

        val speedStat = creature.stat.getAdd(DoubleStat.STAT_BONUS_SPEED).toByte()
        if (speedStat >= 0 && speedStat < BaseStats.values().size) {
            val baseStat: BaseStats = BaseStats.values()[speedStat.toInt()]
            val bonus = max(0.0, baseStat.calcBonus(creature) - 55)
            baseValue += bonus
        }

        val value = defaultValue(creature, stat, calculationType, baseValue)
        val maxValue: Double = if (creature.isPlayer) Config.LIM_MOVE else Config.LIM_MOVE + 50
        return validateValue(creature, value, 1.0, maxValue)
    }

    override fun calcEnchantBodyPartBonus(enchantLevel: Int, isBlessed: Boolean): Double {
        return if (isBlessed) {
            1.0 * max(enchantLevel - 3, 0) + 1.0 * max(enchantLevel - 6, 0)
        } else {
            0.6 * max(enchantLevel - 3, 0) + 0.6 * max(enchantLevel - 6, 0)
        }
    }

    private fun getBaseSpeed(creature: Creature, stat: DoubleStat): Double {
        var baseValue = calcEquippedItemsBaseValue(creature, stat)

        if (creature.isPlayer) {
            val player = requireNotNull(creature.player)
            if (player.isMounted) {
                baseValue = player.mount.getSpeedOnRide(stat)

                // if level diff with mount >= 10, it decreases move speed by 50%
                if (player.mountLevel - creature.level >= 10) {
                    baseValue /= 2.0
                }
                // if mount is hungry, it decreases move speed by 50%
                if (player.mount.isHungry) {
                    baseValue /= 2.0
                }
            }
        }

        if (creature.isPlayable) {
            baseValue += creature.zones.sumByDouble { it.getMoveBonus(creature) }
        }

        return baseValue
    }

}