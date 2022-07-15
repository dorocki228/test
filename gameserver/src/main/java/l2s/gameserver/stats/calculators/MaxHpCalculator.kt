package l2s.gameserver.stats.calculators

import l2s.gameserver.Config
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Player
import l2s.gameserver.model.base.BaseStats
import l2s.gameserver.model.instances.PetInstance
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.tables.EnchantHPBonusTable
import l2s.gameserver.templates.item.ItemQuality
import java.util.function.Predicate

/**
 * @author UnAfraid
 * @author Java-man
 * @since 04.10.2019
 */
object MaxHpCalculator : StatCalculator {
    
    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        var baseValue: Double = when {
            creature.isPlayer -> {
                val player = creature.player
                requireNotNull(player)

                var temp = when {
                    player.isMounted -> player.mount.maxHpOnRide
                        .orElseGet { player.classId.getBaseHp(player.level) }
                    player.isTransformed -> player.transform.getBaseHpMax(player.level)
                        .orElseGet { player.classId.getBaseHp(player.level) }
                    else -> player.classId.getBaseHp(player.level)
                }

                temp += calcEnchantBonusModifier(player)

                temp
            }
            creature.isPet -> {
                val pet = creature as PetInstance
                pet.data.getHP(creature.level)
                    .or { pet.template.getBaseHpMax(pet.level) }
                    .orElse(0.0)
            }
            else -> creature.template.getBaseHpMax(creature.level).orElse(0.0)
        }

        baseValue *= BaseStats.CON.calcBonus(creature)

        val value = defaultValue(creature, stat, calculationType, baseValue)
        val maxValue = if (creature.isPlayer) Config.HP_LIMIT else Double.MAX_VALUE
        return validateValue(creature, value, 1.0, maxValue)
    }

    private fun calcEnchantBonusModifier(player: Player): Double {
        return player.inventory.getPaperdollItems(Predicate { it.enchantLevel > 0 })
            .sumByDouble { item ->
                val hpBonus = EnchantHPBonusTable.getInstance().getHPBonus(player, item)
                val isBlessed = item.template.quality == ItemQuality.BLESSED
                if (isBlessed) {
                    // TODO should work like this ?
                    hpBonus * 1.5
                } else {
                    hpBonus.toDouble()
                }
            }
    }

}