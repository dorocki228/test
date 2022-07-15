package l2s.gameserver.stats.calculators

import l2s.gameserver.model.Creature
import l2s.gameserver.model.base.BaseStats
import l2s.gameserver.stats.DoubleStat

/**
 * @author UnAfraid
 * @author Java-man
 * @since 05.10.2019
 */
object MaxCpCalculator : StatCalculator {

    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        var baseValue = when {
            creature.isPlayer -> {
                val player = creature.player
                requireNotNull(player)

                val temp = when {
                    player.isTransformed -> player.transform.getBaseCpMax(player.level)
                        .orElseGet { player.classId.getBaseCp(player.level) }
                    else -> player.classId.getBaseCp(player.level)
                }

                temp
            }
            else -> creature.template.getBaseCpMax(creature.level).orElse(0.0)
        }

        baseValue *= BaseStats.CON.calcBonus(creature)

        return defaultValue(creature, stat, calculationType, baseValue)
    }

}