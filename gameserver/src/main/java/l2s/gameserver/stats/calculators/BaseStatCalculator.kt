package l2s.gameserver.stats.calculators

import l2s.gameserver.model.Creature
import l2s.gameserver.model.Player
import l2s.gameserver.model.base.BaseStats
import l2s.gameserver.stats.DoubleStat

/**
 * @author UnAfraid
 * @author Java-man
 * @since 01.10.2019
 */
object BaseStatCalculator : StatCalculator {

    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        val baseStat = BaseStats.valueOf(stat)

        val baseValue: Double = when {
            creature.isPlayer -> {
                val player = creature.player
                requireNotNull(player)

                val temp = when {
                    player.isTransformed -> player.transform.getBaseValue(stat)
                        .or { player.template.getBaseValue(stat) }.orElse(0.0)
                    else -> player.template.getBaseValue(stat).orElse(0.0)
                }

                temp + calcHennaModifier(player, baseStat)
            }
            else -> creature.template.getBaseValue(stat).orElse(0.0)
        }

        val value = defaultValue(creature, stat, calculationType, baseValue)
        return validateValue(
            creature,
            value,
            baseStat.getMinValue(creature),
            baseStat.getMaxValue(creature)
        )
    }

    private fun calcHennaModifier(player: Player, stat: BaseStats) =
        player.hennaList.getValue(stat)

}