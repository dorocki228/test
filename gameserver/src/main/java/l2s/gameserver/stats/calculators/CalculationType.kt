package l2s.gameserver.stats.calculators

import l2s.gameserver.model.Creature
import l2s.gameserver.stats.DoubleStat

/**
 * @author Java-man
 */
enum class CalculationType {
    FULL_VALUE {
        override fun calculate(
                creature: Creature,
                stat: DoubleStat,
                baseValue: Double?
        ): Double {
            val passiveMul = creature.stat.getPassiveMul(stat)
            val passiveAdd = creature.stat.getPassiveAdd(stat)

            val mul = creature.stat.getMul(stat)
            val add = creature.stat.getAdd(stat)

            if (baseValue != null) {
                return (baseValue * passiveMul + passiveAdd) * mul + add + creature.stat.getMoveTypeValue(stat, creature.moveType)
            }

            // TODO it is right ?
            return passiveAdd * mul + add + creature.stat.getMoveTypeValue(stat, creature.moveType)
        }
    },
    BASE_VALUE {
        override fun calculate(
                creature: Creature,
                stat: DoubleStat,
                baseValue: Double?
        ): Double {
            val passiveMul = creature.stat.getPassiveMul(stat)
            val passiveAdd = creature.stat.getPassiveAdd(stat)

            if (baseValue != null) {
                return baseValue * passiveMul + passiveAdd
            }

            // TODO it is right ?
            return passiveAdd
        }
    };

    abstract fun calculate(
            creature: Creature,
            stat: DoubleStat,
            baseValue: Double?
    ): Double
}