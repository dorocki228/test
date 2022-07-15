package l2s.gameserver.stats.calculators

import l2s.gameserver.model.Creature
import l2s.gameserver.model.base.BaseStats
import l2s.gameserver.model.instances.PetInstance
import l2s.gameserver.stats.DoubleStat

/**
 * @author UnAfraid
 * @author Java-man
 * @since 04.10.2019
 */
object MaxMpCalculator : StatCalculator {

    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        var baseValue = calcEquippedItemsBaseValue(creature, stat)

        baseValue = creature.getStat().getValue(DoubleStat.MAX_MP_ADD, baseValue)

        baseValue += when {
            creature.isPlayer -> {
                val player = creature.player
                requireNotNull(player)

                val temp = when {
                    player.isMounted -> player.mount.maxMpOnRide
                        .orElseGet { player.classId.getBaseMp(player.level) }
                    player.isTransformed -> player.transform.getBaseMpMax(player.level)
                        .orElseGet { player.classId.getBaseMp(player.level) }
                    else -> player.classId.getBaseMp(player.level)
                }

                temp
            }
            creature.isPet -> {
                val pet = creature as PetInstance
                pet.data.getMP(creature.level)
                    .or { pet.template.getBaseMpMax(pet.level) }
                    .orElse(0.0)
            }
            else -> creature.template.getBaseMpMax(creature.level).orElse(0.0)
        }

        baseValue *= BaseStats.MEN.calcBonus(creature)

        return defaultValue(creature, stat, calculationType, baseValue)
    }

}